/**
 * Common 객체
 */
var Common = {
    location: {}
    , event: {}
    , events: {}
    , init: function () {
    }
    , isNull: function (_param) {
        if (_param === undefined || _param === null) {
            return true;
        } else {
            return false;
        }
    }
    , isNotNull: function (_param) {
        return !this.isNull(_param);
    }
    , error: function (msg) {
        alert(msg);
        throw(msg);
    }
    , eventBind: function (screenObj) {
        var screenId = screenObj;
        if (typeof screenId == 'string') {
            screenObj = window[screenId];
        }

        //[시작] event 바인딩
        if (screenObj.events) {
            var keyList = Object.keys(screenObj.events);

            $(keyList).each(function (idx, key) {
                var keys = $.trim(key);
                var pos = keys.indexOf(' ');
                var evt = keys.substring(0, pos);
                var selecter = keys.substring(pos + 1);
                var func = screenObj.events[keyList[idx]];
                var nameList = func.split('.').slice(1);
                var targetFunc = screenObj;
                nameList.forEach(function (name, idx) {
                    targetFunc = targetFunc[name];
                });
                $('body').off(evt, selecter).on(evt, selecter, targetFunc);
            });
        }
        //[종료] event 바인딩
    }
    , ajaxTransaction: function (_param) {
        return new Promise(function (resolve, reject) {
            if (Common.isNull(_param)) {
                Common.error("param is null");
            }

            if (Common.isNull(_param.url)) {
                Common.error('url is null');
            }

            var reqUrl = _param.url

            $.ajax({
                url: reqUrl
                , type: "POST"
                , async: false
                , success: function (resData) {
                    resolve(resData);
                }
                , error: function (xhr, errorName, error) {
                    reject(xhr);
                }
            });
        });
    }
};