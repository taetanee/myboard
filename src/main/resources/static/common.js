/**
 * Common 객체
 */
var Common = {
    ajaxTransaction : function ( _param ) {
        return new Promise(function (resolve, reject) {
            if(Common.isNull(_param)){
                Common.error("param is null");
            }

            if(Common.isNull(_param.url)){
                Common.error('url is null');
            }

            var reqUrl = _param.url

            $.ajax({
                url : reqUrl
                , type : "POST"
                , async: false
                , success : function (resData) {
                    resolve(resData);
                }
                , error       : function(xhr, errorName, error) {
                    reject(xhr);
                }
            });
        });
    }
    , isNull : function ( _param ) {
        if( _param === undefined || _param === null) {
            return true;
        } else {
            return false;
        }
    }
    , isNotNull : function ( _param ) {
        return !this.isNull(_param);
    }
    , error : function(msg){
        alert(msg);
        throw(msg);
    }
};