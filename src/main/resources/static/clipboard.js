var clipboardPage = $.extend({}, Common);

clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');
}

clipboardPage.events = {
    'click #copyBtn': 'clipboardPage.event.clickedCopyBtn'
}

clipboardPage.event.clickedCopyBtn = function () {
    clipboardPage.event.copy();
}

clipboardPage.event.copy = function () {
    const clipboardValue = $("#clipboard").val();
    window.navigator.clipboard.writeText(clipboardValue).then(() => {
        alert('복사완료');//TODO : 토스트 메세지로 구현하기
    });
}

$(document).ready(function () {
    clipboardPage.init();
});