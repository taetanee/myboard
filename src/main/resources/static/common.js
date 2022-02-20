/**
 * Common 객체
 */
var Common = {
    ajax : function ( _param ) {

        if(this.isNull(_param)){
            this.error("파라미터 null");
        }

        if(this.isNull(_param["url"])){
            this.error('url null');
        }

        var param = _param;

        $.ajax({
            url : param["url"]
            , type : "POST"
            , success : function (result) {

            }
            , error : function () {
                alert("error 발생");
            }
        });
    }
    , isNull : function ( _param ) {
        if( _param === undefined || _param === null) {
            return true;
        } else {
            return true;
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