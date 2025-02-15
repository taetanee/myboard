var clipboardPage = $.extend({}, Common);

clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');

    clipboardPage.location.getRandomWord();
}

clipboardPage.events = {
    'click #copyBtn': 'clipboardPage.event.clickedCopyBtn'
    , 'click #shareBtn': 'clipboardPage.event.clickedShareBtn'
}

clipboardPage.event.clickedCopyBtn = function () {
    clipboardPage.location.copyContent();
}

clipboardPage.event.clickedShareBtn = function () {
    clipboardPage.location.shareURL();
}

clipboardPage.location.copyContent = function () {
    const val = $("#content").val();
    window.navigator.clipboard.writeText(val).then(() => {
        alert('Content copy completed(내용복사완료)');//TODO : 토스트 메세지로 구현하기
    });
}

clipboardPage.location.shareURL = function () {
    const val = window.location.href;
    window.navigator.clipboard.writeText(val).then(() => {
        alert('URL copy completed(URL복사완료)');//TODO : 토스트 메세지로 구현하기
    });
}

clipboardPage.location.getRandomWord = function () {
    var param = {
        url: "/onlineClipboard/getRandomWord"
    };
    clipboardPage.ajaxTransaction(param).then(function (result) {
        const randomWord = result.result;
        const obj = {
            "randomWord" :  randomWord
        }
        Common.setDetails(obj, $(".container"));
        history.pushState(null, null, '?url='+randomWord);
    });
}

$(document).ready(function () {
    clipboardPage.init();
});