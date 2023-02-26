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

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.HtmlLayout;

import java.io.Serializable;

//This code is for Java 1.7 and requires log4j 2.8+ (2.7 is okay if "Core.CATEGORY_NAME" is replaced by string value).

/**
 * Like standard org.apache.logging.log4j.core.appender.SmtpAppender but with
 * some additional features like PatternLayout for subject, so subject changes
 * with each log event, and burst summarizing.
 * Plugin name "SMTPx" is for "SMPT extended".
 *    <br>
 * Mail subject may contain a pattern for PatternLayout; no complete PatternLayout instance
 * is used, thus config. properties patternSelector and replace are not supported (and are not
 * useful in this special case, anyway).
 * You need to set subjectWithLayout to true; this supports "old" configs with specials chars in subject.
 *    <br><br>
 * Burst summarizing (must be enabled by setting parameter burstSummarizingSeconds) works as follows:<ul>
 * <li> the first occurrence is emailed immediately
 * <li> all following similar ERROR logs are buffered for burstSummarizingSeconds (similarity is
 *      configurable with bs* parameters)
 * <li> after burstSummarizingSeconds passed, a summary email with summary info (number of
 *      events, time) together with the first and last event is send.
 * </ul>
 * (Note: This class is nearly copy+paste of original code because SmtpAppender is final (why?).)
 *
 * @see org.apache.logging.log4j.core.appender.SmtpAppender
 * @see #createAppender(String, String, String, String, String, String, String, boolean, String, String, int, String, String, boolean, int, Layout, Filter, boolean, int, char, boolean, int, boolean, boolean, boolean, boolean)
 * @see ExtendedSmtpManager
 *
 * @author authors of original SmtpAppender (much copied from there)
 * @author Thies Wellpott (twapache@online.de)
 */
@Plugin(name = "SMTPx", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class ExtendedSmtpAppender extends AbstractAppender
{

    /** The SMTP Manager */
    private final ExtendedSmtpManager manager;

    private ExtendedSmtpAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                                 final ExtendedSmtpManager manager, final boolean ignoreExceptions, final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.manager = manager;
    }


    /**
     * Create an ExtendedSmtpAppender.
     *
     * @param name  The name of the Appender. Required.
     * @param to  The comma-separated list of recipient email addresses.
     * @param cc  The comma-separated list of CC email addresses.
     * @param bcc  The comma-separated list of BCC email addresses.
     * @param from  The email address of the sender. Required.
     * @param replyTo  The comma-separated list of reply-to email addresses.
     * @param subject The subject as plain text or as pattern for PatternLayout (see subjectWithLayout). Required.
     * @param subjectWithLayout If true, the subject is used as pattern for PatternLayout; default is false.
     * @param smtpProtocol The SMTP transport protocol (such as "smtps", defaults to "smtp").
     * @param smtpHost  The SMTP hostname to send to. Required.
     * @param smtpPort  The SMTP port to send to; default: 25.
     * @param smtpUsername  The username required to authenticate against the SMTP server.
     * @param smtpPassword  The password required to authenticate against the SMTP server.
     * @param smtpDebug  Enable mail session debuging on STDOUT.
     * @param bufferSize  How many log events should be buffered for inclusion in the message? Default is 10.
     * @param layout  The layout to use (defaults to HtmlLayout).
     * @param filter  The Filter or null (defaults to ThresholdFilter, level of ERROR).
     * @param ignoreExceptions  If {@code "true"} (the default) exceptions encountered when appending events are logged;
     *                          otherwise they are propagated to the caller.
     * @param burstSummarizingSeconds  Number of seconds to summarize similar log messages over.
     *                                 &lt;= 0 to disable this feature (the default).
     * @param bsCountInSubject  Shall the number of summarized events be put in the subject?
     *                          'F' or 'S' for at front/start, 'B' or 'E' for behind/at end;
     *                          default is no count in subject.
     * @param bsLoggername  For summarizing the logger name is relevant; default: false.
     * @param bsMessagePrefixLength  For summarizing this number of characters from the beginning of the message text are relevant; default: 30.
     * @param bsMessageMaskDigits  For summarizing, digits in the message text shall be masked, so their concrete value is irrelevant; default: false.
     * @param bsExceptionClass  For summarizing the exception class name is relevant; default: true.
     * @param bsExceptionOrigin  For summarizing the first line of the exception stack trace is relevant; default: false.
     * @param bsRootExceptionClass  For summarizing the class name of the root cause is relevant; default: false.
     *
     * @return The newly created ExtendedSmtpAppender. null on error.
     */
    @SuppressWarnings("resource")		// manager is created here but closed in another method
    @PluginFactory
    public static ExtendedSmtpAppender createAppender(
            @PluginAttribute("name") @Required(message="SMTP.name is missing") final String name,
            @PluginAttribute("to") final String to,
            @PluginAttribute("cc") final String cc,
            @PluginAttribute("bcc") final String bcc,
            @PluginAttribute("from") @Required(message="SMTP.from is missing") final String from,
            @PluginAttribute("replyTo") final String replyTo,
            @PluginAttribute("subject") @Required(message="SMTP.subject is missing") final String subject,
            @PluginAttribute("subjectWithLayout") final boolean subjectWithLayout,
            @PluginAttribute(value="smtpProtocol", defaultString="smtp") final String smtpProtocol,
            @PluginAttribute(value="useStartTls", defaultBoolean=false) final boolean useStartTls,
            @PluginAttribute("smtpHost") @Required(message="SMTP.smtpHost is missing") final String smtpHost,
            @PluginAttribute(value="smtpPort", defaultInt=25) final int smtpPort,
            @PluginAttribute("smtpUsername") final String smtpUsername,
            @PluginAttribute("smtpPassword") final String smtpPassword,
            @PluginAttribute("smtpDebug") final boolean smtpDebug,
            @PluginAttribute(value="bufferSize", defaultInt=10) final int bufferSize,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter,
            @PluginAttribute(value="ignoreExceptions", defaultBoolean=true) final boolean ignoreExceptions,
            @PluginAttribute("burstSummarizingSeconds") final int burstSummarizingSeconds,
            @PluginAttribute("bsCountInSubject") final char bsCountInSubject,
            @PluginAttribute("bsLoggername") final boolean bsLoggername,
            @PluginAttribute(value="bsMessagePrefixLength", defaultInt=30) final int bsMessagePrefixLength,
            @PluginAttribute("bsMessageMaskDigits") final boolean bsMessageMaskDigits,
            @PluginAttribute(value="bsExceptionClass", defaultBoolean=true) final boolean bsExceptionClass,
            @PluginAttribute("bsExceptionOrigin") final boolean bsExceptionOrigin,
            @PluginAttribute("bsRootExceptionClass") final boolean bsRootExceptionClass
            ) {

        if (layout == null) {
            layout = HtmlLayout.createDefaultLayout();
        }
        if (filter == null) {
            filter = ThresholdFilter.createFilter(null, null, null);
        }

        final ExtendedSmtpManager manager = ExtendedSmtpManager.getSmtpManager(
                new ExtendedSmtpManager.FactoryData(to, cc, bcc, from, replyTo, subject, subjectWithLayout,
                        smtpProtocol, useStartTls, smtpHost, smtpPort, smtpUsername, smtpPassword, smtpDebug, bufferSize,
                        burstSummarizingSeconds*1000L, bsCountInSubject, bsLoggername,
                        bsMessagePrefixLength, bsMessageMaskDigits,
                        bsExceptionClass, bsExceptionOrigin, bsRootExceptionClass),
                filter.toString(), layout.getContentType());
        if (manager == null) {
            return null;
        }

        return new ExtendedSmtpAppender(name, filter, layout, manager, ignoreExceptions, null);
    }


    @Override
    public void stop() {
        super.stop();
        manager.close();			// important here to allow stopping the background thread
    }


    /**
     * Capture all events in CyclicBuffer.
     * @param event The Log event.
     * @return true if the event should be filtered.
     */
    @Override
    public boolean isFiltered(final LogEvent event) {
        final boolean filtered = super.isFiltered(event);
        if (filtered) {
            // To do using garbage free logging the buffered event will be reused/overwritten,
            // so do a clone() when not using AsyncAppender (how to recognize?).
            // BUT: this appender is - in real world - always used with AsyncAppender,
            // so do not do the copy twice.
            // Possibly relevant code from AsyncAppender:
            //final Log4jLogEvent memento = Log4jLogEvent.createMemento(logEvent, includeLocation);
            //InternalAsyncUtil.makeMessageImmutable(logEvent.getMessage());
            manager.add(event);
        }
        return filtered;
    }

    /**
     * Perform SmtpAppender specific appending actions, mainly adding the event
     * to a cyclic buffer and checking if the event triggers an e-mail to be
     * sent.
     * @param event The Log event.
     */
    @Override
    public void append(final LogEvent event) {
        manager.sendEvents(getLayout(), event);
    }

}
