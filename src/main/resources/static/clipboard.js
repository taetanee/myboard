var clipboardPage = $.extend({}, CommonObject);

clipboardPage.variable = {
    sendData: {}
    , detailData: {}
}


clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');

    if (CommonObject.isNull(CommonObject.getQueryParam('url'))) {
        clipboardPage.location.getRandomWord();
    } else {
        CommonObject.setDetails({"randomWord": CommonObject.getQueryParam('url')}, $(".container"));
        clipboardPage.location.getContent();
    }
}

clipboardPage.events = {
    'click #copyBtn': 'clipboardPage.event.clickedCopyBtn'
    , 'click #shareBtn': 'clipboardPage.event.clickedShareBtn'
    , 'click #saveBtn': 'clipboardPage.event.clickedSaveBtn'
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
        CommonObject.setDetails(obj, $(".container"));
        history.pushState(null, null, '?url='+randomWord);
    });
}

clipboardPage.location.getContent = function () {
    var param = {
        url: "/onlineClipboard/getContent"
        , data : clipboardPage.makeInputData($(".container"), clipboardPage.variable.sendData)
    };
    clipboardPage.ajaxTransaction(param).then(function (result) {
        const content = result.result;
        const obj = {
            "content" :  content.data
        }
        CommonObject.setDetails(obj, $(".container"));
    });
}

clipboardPage.event.clickedSaveBtn = function () {
    var param = {
        url: "/onlineClipboard/saveContent"
        , data : clipboardPage.makeInputData($(".container"), clipboardPage.variable.sendData)
    };
    clipboardPage.ajaxTransaction(param).then(function (result) {
        alert('content save completed(내용 저장 완료)');//TODO : 토스트 메세지로 구현하기
    });
}

$(document).ready(function () {
    clipboardPage.init();
});