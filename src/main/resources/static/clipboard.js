var clipboardPage = $.extend({}, Common);

clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');

    clipboardPage.location.getRandomWord();
}

clipboardPage.events = {
    'click #copyBtn': 'clipboardPage.event.clickedCopyBtn'
}

clipboardPage.event.clickedCopyBtn = function () {
    clipboardPage.location.copy();
}

clipboardPage.location.copy = function () {
    const clipboardValue = $("#clipboard").val();
    window.navigator.clipboard.writeText(clipboardValue).then(() => {
        alert('복사완료');//TODO : 토스트 메세지로 구현하기
    });
}

clipboardPage.location.getRandomWord = function () {
    var param = {
        url: "/onlineClipboard/getRandomWord"
    };
    clipboardPage.ajaxTransaction(param).then(function (result) {
        const randomWord = {
            "random_word" :  result.result[0]["word"] + " " + result.result[1]["word"]
        }
        Common.setDetails(randomWord, $(".container"));
    });
}

$(document).ready(function () {
    clipboardPage.init();
});