var clipboardPage = CommonObject.clone();

clipboardPage.variable = {
    sendData: {}
    , detailData: {}
}


clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');

    if (CommonObject.isNull(CommonObject.getUrlParams('url'))) {
        clipboardPage.location.getRandomWord();
    } else {
        CommonObject.setDetails({"randomWord": CommonObject.getUrlParams('url')}, $(".container"));
        clipboardPage.location.getContent();
    }
}

clipboardPage.events = {
    'click #fileUploadBtn'      : 'clipboardPage.event.clickedFileUploadBtn'
    , 'click #fileDownloadBtn'  : 'clipboardPage.event.clickedFileDownloadBtn'
    , 'click #copyBtn'          : 'clipboardPage.event.clickedCopyBtn'
    , 'click #shareBtn'         : 'clipboardPage.event.clickedShareBtn'
    , 'click #saveBtn'          : 'clipboardPage.event.clickedSaveBtn'
}

clipboardPage.event.clickedFileUploadBtn = function () {
    const obj = {
        fileInputId: "fileInput"
        , fileListId: "fileList"
        , uploadUrl: "/onlineClipboard/upload"
        , fileParamName: "file"
    }
    clipboardPage.fileUpload(obj)
}

clipboardPage.event.clickedFileDownloadBtn = function () {
    const obj = {
        fileName: "x.txt"
        , downloadUrl: "/onlineClipboard/download"
    }
    clipboardPage.fileDownload(obj);
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
        CommonObject.toast('Content copy completed(내용복사완료)');
    });
}

clipboardPage.location.shareURL = function () {
    const val = window.location.href;
    window.navigator.clipboard.writeText(val).then(() => {
        CommonObject.toast('URL copy completed(URL복사완료)');
    });
}

clipboardPage.location.getRandomWord = function () {
    var param = {
        url: "/onlineClipboard/getRandomWord"
    };
    clipboardPage.ajax(param).then(function (result) {
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
    clipboardPage.ajax(param).then(function (result) {
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
    clipboardPage.ajax(param).then(function (result) {
        CommonObject.toast('content save completed(내용 저장 완료)');
    });
}

$(document).ready(function () {
    clipboardPage.init();
});