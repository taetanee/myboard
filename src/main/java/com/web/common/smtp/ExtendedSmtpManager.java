/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package com.web.common.smtp;

import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.MimeMessageBuilder;
import org.apache.logging.log4j.core.util.CyclicBuffer;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.util.PropertiesUtil;

import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

// This code is for Java 1.7 and requires log4j 2.7+

/**
 * Like standard org.apache.logging.log4j.core.appender.SmtpManager but with
 * some additional features (subject with layout, burst summarizing).
 *    <br>
 * Note: This class is based on copy+paste of original code because SmtpManager contains
 * a lot of private stuff which needs slight changes.
 *
 * @see ExtendedSmtpAppender
 * @author authors of original SmtpManager (basis copied from there)
 * @author Thies Wellpott (twapache@online.de)
 */
public class ExtendedSmtpManager extends AbstractManager
{
    private static final SMTPManagerFactory FACTORY = new SMTPManagerFactory();

    private final FactoryData data;
    private final PatternLayout subjectLayout;

    private final Thread summarySender;
    private final CyclicBuffer<LogEvent> buffer;
    private final Session session;
    private volatile MimeMessage message;



    /** Create instance. Internal use, for public creation use getSMTPManager() */
    protected ExtendedSmtpManager(final String name, final Session session, final MimeMessage message,
                          final FactoryData data) {
        super(null, name);
        this.session = session;
        this.message = message;
        this.data = data;
        if (data.subjectWithLayout) {
            this.subjectLayout = PatternLayout.newBuilder().withPattern(data.subject).withAlwaysWriteExceptions(false).build();
        } else {
            this.subjectLayout = null;
        }
        this.buffer = new CyclicBuffer<>(LogEvent.class, data.numElements);
        // create and start background thread
        this.summarySender = startSummarySenderBackgroundThread();
    }


    /**
     * Factory method: get existing or create a new manager for SMTP messages.
     * @param data  parameter data
     * @param contentType  MIME content type of the layout, e.g. "text/plain" or "text/html". May be null.
     */
    public static ExtendedSmtpManager getSmtpManager(final FactoryData data, final String filterName, String contentType) {
        final StringBuilder sb = new StringBuilder();
        if (data.to != null) {
            sb.append(data.to);
        }
        sb.append(':');
        if (data.cc != null) {
            sb.append(data.cc);
        }
        sb.append(':');
        if (data.bcc != null) {
            sb.append(data.bcc);
        }
        sb.append(':');
        if (data.from != null) {
            sb.append(data.from);
        }
        sb.append(':');
        if (data.replyto != null) {
            sb.append(data.replyto);
        }
        sb.append(':');
        if (data.subject != null) {
            sb.append(data.subject);
        }
        sb.append(':').append(data.burstSummarizingMillis);
        sb.append(':').append(data.bsCountInSubject);
        sb.append(':').append(data.bsMessagePrefixLength).append(data.bsMessageMaskDigits);
        sb.append(':').append(data.bsLoggername).append(data.bsExceptionClass).append(data.bsExceptionOrigin)
          .append(data.bsRootExceptionClass);
        sb.append(':');
        sb.append(data.protocol).append(':').append(data.host).append(':').append(data.port);
        sb.append(':').append(data.username).append(':').append(data.password);		// values may be null
        sb.append(data.isDebug ? ":debug:" : "::");
        sb.append(filterName);

        final String name = "SMTP:" + NameUtil.md5(sb.toString());

        ExtendedSmtpManager manager = getManager(name, FACTORY, data);
        if (contentType != null  &&  contentType.length() > 0) {
            manager.lastContentType = contentType;		// init from current layout
        }
        return manager;
    }


    /** "Close" the manager: stop the background thread and wait for its end (typically is very fast). */
    @Override
    protected boolean releaseSub(long timeout, TimeUnit timeUnit) {
        summarySender.interrupt();
        try {		// TODO? use timeout??
            summarySender.join(2000);		// if thread is currently sending emails, give it some time to finish
        } catch (InterruptedException e) {
            ; // ignore
        }
        checkSendSummary(null);				// final sending of remaining buffered emails
        return true;
    }


    /** Add event to internal buffer. */
    public void add(final LogEvent event) {
        buffer.add(event);
    }


    /**
     * Send the contents of the cyclic buffer as an e-mail message.
     * @param layout The layout for formatting the events.
     * @param appendEvent The event that triggered the send.
     */
    public void sendEvents(final Layout<?> layout, final LogEvent appendEvent) {
        checkSendSummary(layout);		// always send buffered emails before new events; also update layout/content type
        if (message == null) {
            connect();
        }
        try {
            // always empty the buffered events, create message text and subject
            final LogEvent[] priorEvents = buffer.removeAll();
            final byte[] rawBytes = formatContentToBytes(priorEvents, appendEvent, layout);
            String newSubject = null;
            if (subjectLayout != null) {
                newSubject = subjectLayout.toSerializable(appendEvent);
            }

            SummarizeData sumData = summarizeEvent(appendEvent);
            if (sumData != null) {
                // record data
                if (sumData.secondEventMsg == null) {
                    sumData.secondEventMsg = rawBytes;
                }
                sumData.lastEventMsg = rawBytes;
                sumData.lastSubject = newSubject;		// note: this may be null
            } else {
                // send message
                final String contentType = layout.getContentType();
                final String encoding = getEncoding(rawBytes, contentType);
                final byte[] encodedBytes = encodeContentToBytes(rawBytes, encoding);
                final InternetHeaders headers = getHeaders(contentType, encoding);
                final MimeMultipart mp = getMimeMultipart(encodedBytes, headers);
                sendMultipartMessage(message, newSubject, mp);
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred while sending e-mail notification.", e);
            throw new LoggingException("Error occurred while sending email", e);
        }
    }


    protected byte[] formatContentToBytes(final LogEvent[] priorEvents, final LogEvent appendEvent,
                                          final Layout<?> layout) throws IOException {
        final ByteArrayOutputStream raw = new ByteArrayOutputStream();
        writeContent(priorEvents, appendEvent, layout, raw);
        return raw.toByteArray();
    }

    private void writeContent(final LogEvent[] priorEvents, final LogEvent appendEvent, final Layout<?> layout,
                              final ByteArrayOutputStream out)
        throws IOException {
        writeHeader(layout, out);
        writeBuffer(priorEvents, appendEvent, layout, out);
        writeFooter(layout, out);
    }

    protected void writeHeader(final Layout<?> layout, final OutputStream out) throws IOException {
        final byte[] header = layout.getHeader();
        if (header != null) {
            out.write(header);
        }
    }

    protected void writeBuffer(final LogEvent[] priorEvents, final LogEvent appendEvent, final Layout<?> layout,
                               final OutputStream out) throws IOException {
        for (final LogEvent priorEvent : priorEvents) {
            final byte[] bytes = layout.toByteArray(priorEvent);
            out.write(bytes);
        }

        final byte[] bytes = layout.toByteArray(appendEvent);
        out.write(bytes);
    }

    protected void writeFooter(final Layout<?> layout, final OutputStream out) throws IOException {
        final byte[] footer = layout.getFooter();
        if (footer != null) {
            out.write(footer);
        }
    }


    protected String getEncoding(final byte[] rawBytes, final String contentType) {
        final DataSource dataSource = new ByteArrayDataSource(rawBytes, contentType);
        return MimeUtility.getEncoding(dataSource);
    }

    protected byte[] encodeContentToBytes(final byte[] rawBytes, final String encoding)
        throws MessagingException, IOException {
        final ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        encodeContent(rawBytes, encoding, encoded);
        return encoded.toByteArray();
    }

    protected void encodeContent(final byte[] bytes, final String encoding, final ByteArrayOutputStream out)
            throws MessagingException, IOException {
        try (OutputStream encoder = MimeUtility.encode(out, encoding)) {
            encoder.write(bytes);
        }
    }

    protected InternetHeaders getHeaders(final String contentType, final String encoding) {
        final InternetHeaders headers = new InternetHeaders();
        headers.setHeader("Content-Type", contentType + "; charset=UTF-8");		//XXX layout.getCharset() would be great here, this way the layout explicitly must use this charset!
        headers.setHeader("Content-Transfer-Encoding", encoding);
        return headers;
    }

    protected MimeMultipart getMimeMultipart(final byte[] encodedBytes, final InternetHeaders headers)
        throws MessagingException {
        final MimeMultipart mp = new MimeMultipart();
        final MimeBodyPart part = new MimeBodyPart(headers, encodedBytes);
        mp.addBodyPart(part);
        return mp;
    }

    /**
     */
    protected void addMimeMultipart(final MimeMultipart mp, final byte[] rawBytes, final String contentType)
            throws MessagingException, IOException {
        String encoding = getEncoding(rawBytes, contentType);
        byte[] encodedBytes = encodeContentToBytes(rawBytes, encoding);
        InternetHeaders headers = getHeaders(contentType, encoding);
        mp.addBodyPart(new MimeBodyPart(headers, encodedBytes));
    }


    /** Send the email message. Set subject if not null. */
    protected void sendMultipartMessage(final MimeMessage msg, String subject, final MimeMultipart mp) throws MessagingException {
        synchronized (msg) {
            String prevSubjectHeader = null;
            if (subject != null) {
                prevSubjectHeader = msg.getHeader("Subject", null);		// use encoded header value instead of getSubject()
                subject = subject.replace('\n', ' ');					// LF is not allowed in subject
                msg.setSubject(subject, "UTF-8");	// would be great, but is protected (and both use platform default charset): subjectLayout.getCharset());
            }
            msg.setContent(mp);
            msg.setSentDate(new Date());
            Transport.send(msg);
            if (prevSubjectHeader != null  &&  !data.subjectWithLayout) {
                // reset subject to old value, but only when no layout-subject is used (each msg gets its own subject then)
                msg.setHeader("Subject", prevSubjectHeader);
            }
        } // sync
    }


    /** Create message initialized with static data. But only, if not already present. */
    private synchronized void connect() {
        if (message != null) {
            return;
        }
        try {
            message = new MimeMessageBuilder(session).setFrom(data.from).setReplyTo(data.replyto)
                .setRecipients(Message.RecipientType.TO, data.to).setRecipients(Message.RecipientType.CC, data.cc)
                .setRecipients(Message.RecipientType.BCC, data.bcc).setSubject(data.subject).build();
        } catch (final MessagingException e) {
            LOGGER.error("Could not set SmtpAppender message options.", e);
            message = null;
        }
    }


////////////////  Burstfilter


    // for sendSummary()
    // SimpleDateFormat is not thread safe, so use commons-lang/FastDateFormat (but not here
    // because of unwanted dependency):
    //private static final FastDateFormat dfTime = FastDateFormat.getInstance("HH:mm:ss");

    /** Collects the email data. Key is getEventSummarizeKey(), value is data for this summary. */
    private Map<String, SummarizeData> summarizeDataCollector = new HashMap<>();

    // for getEventSummarizeKey(): find all consecutive digits
    private static final Pattern digitPattern = Pattern.compile("\\d+");


    /** Create and start background thread that periodically calls checkSendSummary(). */
    private Thread startSummarySenderBackgroundThread() {
        // do not summarize if not requested
        if (data.burstSummarizingMillis <= 0)
            return null;

        Thread thread = new Thread() {
                @Override
                public void run() {
                    final long sleepTime = Math.max(data.burstSummarizingMillis / 10, 500);
                    LOGGER.debug("SMTPx background thread {} started, sleep ms {}", getName(), sleepTime);
                    try {
                        // at beginning sleep for collection time (nothing to do during this)
                        sleep(data.burstSummarizingMillis);
                        while (!interrupted()) {
                            sleep(sleepTime);
                            checkSendSummary(null);
                        }
                    } catch (InterruptedException e) {
                        ; // ignore, silently end the thread in this case
                    }
                    LOGGER.debug("SMTPx background thread {} ended.", getName());
                } // run()
            };
        thread.setDaemon(true);
        thread.setName(this.getClass().getSimpleName() + "-" + getName());
        thread.start();
        return thread;
    }


    /**
     * Generate string key for the logging event.
     */
    private String getEventSummarizeKey(LogEvent event) {
        StringBuilder sb = new StringBuilder(300);

        // logger name
        if (data.bsLoggername) {
            sb.append("~~LgNm:").append(event.getLoggerName());		// note: this may be null
        }
        // first part of message text
        if (data.bsMessagePrefixLength > 0) {
            String msgText = event.getMessage().getFormattedMessage();
            if (msgText != null) {		// is typically so, but be defensive
                if (data.bsMessageMaskDigits) {
                    msgText = digitPattern.matcher(msgText).replaceAll("#");	// mask out all digits
                }
                if (msgText.length() > data.bsMessagePrefixLength) {				// restrict length after(!) all replacements
                    msgText = msgText.substring(0, data.bsMessagePrefixLength);
                }
            }
            sb.append("~~Msg:").append(msgText);
        }
        Throwable eventEx = event.getThrown();		// note: getMessage().getThrowable() is always null for simple string
        if (eventEx != null) {
            if (data.bsExceptionClass) {
                // exception class
                sb.append("~~ExCl:").append(eventEx.getClass().getName());
            }
            if (data.bsExceptionOrigin) {
                // exception occurence: first stacktrace line
                StackTraceElement[] stackTrace = eventEx.getStackTrace();
                if (stackTrace != null  &&  stackTrace.length > 0  && stackTrace[0] != null) {
                    sb.append("~~ExO:").append(stackTrace[0].toString());
                }
            }
            if (data.bsRootExceptionClass) {
                // root exception class
                Throwable rootEx = eventEx;
                while (true /*rootEx != null*/) {
                    Throwable cause = rootEx.getCause();
                    if (cause == null  ||  cause == rootEx)
                        break;
                    rootEx = cause;
                } // while
                sb.append("~~RExCl:").append(rootEx.getClass().getName());
            } // if
        } // if (exception present)

        return sb.toString();
    }


    /**
     * Summarize event data. Creates new sum data if needed.
     * Returns null, when event should be sent out directly. Otherwise sum data
     * is returned, whose data should be set/updated from the current event.
     */
    private SummarizeData summarizeEvent(LogEvent logEvent) {
        // do not summarize if not requested
        if (data.burstSummarizingMillis <= 0)
            return null;

        final String eventKey = getEventSummarizeKey(logEvent);
        synchronized (summarizeDataCollector) {
            SummarizeData sumData = summarizeDataCollector.get(eventKey);
            if (sumData == null) {
                // first event with this key, create new summary data (with numOfMsg == 0)
                sumData = new SummarizeData(eventKey, logEvent.getTimeMillis());
                summarizeDataCollector.put(eventKey, sumData);
                return null;		// first event should always be sent
            } else {
                sumData.lastEventMillis = logEvent.getTimeMillis();
                sumData.numOfMsg++;
                if (sumData.numOfMsg == 1) {
                    sumData.secondEventMillis = sumData.lastEventMillis;
                }
                return sumData;
            }
        } // synchronized
    }


    /**
     * Check if summary has to be sent and do so.
     * Performs cleanup of summarizeDataCollector, too (e.g. old messages).
     * Contains fast check to not execute very often, so can be called very
     * often without penalty.
     */
    private void checkSendSummary(final Layout<?> layout) {
        if (data.burstSummarizingMillis <= 0)
            return;
        final long now = System.currentTimeMillis();
        if (now - lastSummaryCheckMillis < data.burstSummarizingMillis / 10)
            return;
        lastSummaryCheckMillis = now;
        if (LOGGER.isTraceEnabled()) {		// check because of "expensive" getTimeInstance()
            LOGGER.trace("SMTPx.checkSendSummary() exec - {}={}", now,
                    DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now));
        }
        if (layout != null) {		// init this value here
            lastContentType = layout.getContentType();
        }
        List<SummarizeData> toSend = new ArrayList<>();
        LOGGER.trace("  - collector before: {}", summarizeDataCollector);
        synchronized (summarizeDataCollector) {
            for (Iterator<Entry<String, SummarizeData>> iter = summarizeDataCollector.entrySet().iterator();  iter.hasNext(); ) {
                SummarizeData sumData = iter.next().getValue();
                if (sumData.numOfMsg == 0  &&  now - sumData.firstEventMillis > data.burstSummarizingMillis) {
                    // only first message recorded which is too old now, simply remove entry
                    iter.remove();
                } else if (sumData.numOfMsg > 0  &&  now - sumData.secondEventMillis > data.burstSummarizingMillis) {
                    // time from first collected message to now is too long, send message (see
                    // below) and remove collected info
                    toSend.add(sumData);
                    iter.remove();
                }
            } // for
            // for all emails to send, add a new info with last message as first message time, so
            // next message with same content will not be sent as first one on its own
            for (SummarizeData sumData : toSend) {
                summarizeDataCollector.put(sumData.sumKey, new SummarizeData(sumData.sumKey, sumData.lastEventMillis));
            }
        } // synchronized
        LOGGER.trace("  - collector afterwards: {}, toSend=#{}", summarizeDataCollector, toSend.size());
        // perform (expensive) sending outside of sync. block
        /* sort toSend according to sumData.secondEventMillis
        toSend.sort(new Comparator<SummarizeData>() {
                @Override
                public int compare(SummarizeData sd1, SummarizeData sd2) {
                    return Long.compare(sd1.secondEventMillis, sd2.secondEventMillis);
                }
            });*/
        for (SummarizeData sumData : toSend) {
            sendSummary(sumData);
        }
    }

    private volatile long lastSummaryCheckMillis = System.currentTimeMillis();	// creation time is the starting point
    private String lastContentType = "text/plain";		// reasonable default value, will be overwritten in most cases


    /** Send summary email. */
    public void sendSummary(SummarizeData sumData) {
        LOGGER.debug("SMTPx.sendSummary() - {}", sumData);
        if (message == null) {
            connect();
        }
        try {
            final String eventContentType = lastContentType;	// geht hier nicht direkt: layout.getContentType();
            final MimeMultipart mp = new MimeMultipart();
            if (sumData.numOfMsg >= 2) {
                final DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                // append summary header information text (if at least two messages present)
                String str = "*** 유사한 에러로그 "
                		+ ((sumData.lastEventMillis - sumData.secondEventMillis + 500) / 1000)
                		+ " 초 동안: " + sumData.numOfMsg + " 개 발생 ***\n"
                		+ "first at " + dfTime.format(sumData.secondEventMillis)
                        + ",  last at " + dfTime.format(sumData.lastEventMillis)
                        + ".\n첫 번째 및 마지막 로그가 첨부됩니다.\n"
                        + "(summary based on:  \"" + sumData.sumKey + "\")\n";
                addMimeMultipart(mp, str.getBytes("UTF-8"), "text/plain");
                // append second log event
                addMimeMultipart(mp, sumData.secondEventMsg, eventContentType);
            } else {
                // give a hint because email (and thus the log) comes delayed
                addMimeMultipart(mp, "*** Single collected summarized log event".getBytes("UTF-8"), "text/plain");
            } // if
            // and the last (or only) log event
            addMimeMultipart(mp, sumData.lastEventMsg, eventContentType);

            String newSubject = sumData.lastSubject;		// is only set for subject with layout
            if (data.bsCountInSubject != '\0'  &&  sumData.numOfMsg > 1) {
                if (newSubject == null) {
                    newSubject = data.subject;		// do not use message.getSubject() because it is expensive
                }
                if (data.bsCountInSubject == 'F'  ||  data.bsCountInSubject == 'S') {
                    newSubject = sumData.numOfMsg + "x  " + newSubject;
                } else {
                    newSubject = newSubject + "  [" + sumData.numOfMsg + "x]";
                }
            } // if

               sendMultipartMessage(message, newSubject, mp);
        } catch (final Throwable e) {
            LOGGER.error("Error occurred while sending summary e-mail notification.", e);
            throw new LoggingException("Error occurred while sending summary email", e);
        }
    }


//////////  inner classes


    /** Summary data. Directly access the fields. */
    private static class SummarizeData
    {
        /** Key in the collector map. */
        final String sumKey;
        /** Time when first (the always sent) event occurred. */
        final long firstEventMillis;
        /** Number of collected messages (the first that is always sent does not count here). */
        int numOfMsg;
        /** Time when second event occurred. */
        long secondEventMillis;
        /** Formatted message text of the second log event. */
        byte[] secondEventMsg;
        /** Time when second event occurred. */
        long lastEventMillis;
        /** Formatted message text of the last collected log event. */
        byte[] lastEventMsg;
        /** Subject of last message; may be null if constant. */
        String lastSubject;

        SummarizeData(String sumKey, long firstEventMillis) {
            this.sumKey = sumKey;
            this.firstEventMillis = firstEventMillis;
        }

        @Override
        @SuppressWarnings("null")		// I use (Eclipse) null checking and Object.toString() is marked @NonNull; but don't put this into a public project
        public String toString() {
            final DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);
            return "SumData[#" + numOfMsg + " / " + firstEventMillis + "=" + dfTime.format(firstEventMillis)
                    + " / " + secondEventMillis + "=" + dfTime.format(secondEventMillis)
                    + " / " + lastEventMillis + "=" + dfTime.format(lastEventMillis)
                    + " / " + lastSubject + "]";
        }

    } // inner class


    /** Factory data: simple data collection of config attributes. */
    public static class FactoryData
    {
        private final String to;
        private final String cc;
        private final String bcc;
        private final String from;
        private final String replyto;
        private final String subject;
        private final boolean subjectWithLayout;
        private final String protocol;
        private final boolean useStartTls;
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final boolean isDebug;
        private final int numElements;
        /** <= 0 for no burst summarizing. */
        private final long burstSummarizingMillis;
        /** \0 for no count info, F/S for front/start, other (B/E) for behind/end. */
        private final char bsCountInSubject;
        // config which parameters of a log event to consider
        private final boolean bsLoggername;
        /** <= 0 for no message. */
        private final int bsMessagePrefixLength;
        private final boolean bsMessageMaskDigits;
        private final boolean bsExceptionClass;
        private final boolean bsExceptionOrigin;
        private final boolean bsRootExceptionClass;

        public FactoryData(String to, String cc, String bcc, String from,
                String replyto, String subject, boolean subjectWithLayout,
                String protocol, boolean useStartTls, String host, int port,
                String username, String password, boolean isDebug,
                int numElements,
                long burstSummarizingMillis, char bsCountInSubject, boolean bsLoggername,
                int bsMessagePrefixLength, boolean bsMessageMaskDigits, boolean bsExceptionClass,
                boolean bsExceptionOrigin, boolean bsRootExceptionClass) {
            this.to = to;
            this.cc = cc;
            this.bcc = bcc;
            this.from = from;
            this.replyto = replyto;
            this.subject = subject;
            this.subjectWithLayout = subjectWithLayout;
            this.protocol = protocol;
            this.useStartTls = useStartTls;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.isDebug = isDebug;
            this.numElements = numElements;
            this.burstSummarizingMillis = burstSummarizingMillis;
            this.bsCountInSubject = Character.toUpperCase(bsCountInSubject);
            this.bsLoggername = bsLoggername;
            this.bsMessagePrefixLength = bsMessagePrefixLength;
            this.bsMessageMaskDigits = bsMessageMaskDigits;
            this.bsExceptionClass = bsExceptionClass;
            this.bsExceptionOrigin = bsExceptionOrigin;
            this.bsRootExceptionClass = bsRootExceptionClass;
        }

    } // inner class


    /**
     * Factory to create the SMTP Manager.
     */
    private static class SMTPManagerFactory implements ManagerFactory<ExtendedSmtpManager, FactoryData>
    {
        @Override
        public ExtendedSmtpManager createManager(final String name, final FactoryData data) {
            final String prefix = "mail." + data.protocol;

            final Properties properties = PropertiesUtil.getSystemProperties();
            properties.put("mail.transport.protocol", data.protocol);
            if (properties.getProperty("mail.host") == null) {
                // Prevent an UnknownHostException in Java 7
                properties.put("mail.host", NetUtils.getLocalHostname());
            }
            if (data.useStartTls) {
            	properties.put("mail.smtp.starttls.enable", "true");
            }

            if (data.host != null) {
                properties.put(prefix + ".host", data.host);
            }
            if (data.port > 0) {
                properties.put(prefix + ".port", String.valueOf(data.port));
            }

            final Authenticator authenticator = buildAuthenticator(data.username, data.password);
            if (authenticator != null) {
                properties.put(prefix + ".auth", "true");
            }

            final Session session = Session.getInstance(properties, authenticator);
            session.setProtocolForAddress("rfc822", data.protocol);
            session.setDebug(data.isDebug);
            MimeMessage message;

            try {
                message = new MimeMessageBuilder(session)
                        .setFrom(data.from).setReplyTo(data.replyto)
                        .setRecipients(Message.RecipientType.TO, data.to)
                        .setRecipients(Message.RecipientType.CC, data.cc)
                        .setRecipients(Message.RecipientType.BCC, data.bcc)
                        .setSubject(data.subject).build();
            } catch (final MessagingException e) {
                LOGGER.error("Could not set SmtpAppender message options.", e);
                message = null;
            }

            return new ExtendedSmtpManager(name, session, message, data);
        }

        private Authenticator buildAuthenticator(final String username, final String password) {
            if (null != password  &&  null != username) {
                return new Authenticator() {
                    private final PasswordAuthentication passwordAuthentication =
                        new PasswordAuthentication(username, password);

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return passwordAuthentication;
                    }
                };
            }
            return null;
        }

    } // inner class Factory

}