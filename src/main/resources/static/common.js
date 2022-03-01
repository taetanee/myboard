/**
 * Common 객체
 */
var Common = {
    ajax : function ( _param ) {

        if(this.isNull(_param)){
            this.error("param is null");
        }

        if(this.isNull(_param["url"])){
            this.error('url is null');
        }

        var param = _param;
        var result = undefined;

        $.ajax({
            url : param["url"]
            , type : "POST"
            , async: false
            , success : function (ajaxResult) {
                result = ajaxResult;
            }
            , error : function () {
                alert("error 발생");
            }
        });
        return result;
    }
    , isNull : function ( _param ) {
        if( _param === undefined || _param === null) {
            return true;
        } else {
            return false;
        }
    }
    , isNotNull : function () {
        return !this.isNull();
    }
    , error : function(msg){
        alert(msg);
        throw(msg);
    }
};