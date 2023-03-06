$(document).ready(function (){
    var indexPage = $.extend({}, Common);

    var param = {
        url : "/exceptionTest"
    }

    indexPage.getData = function () {
        indexPage.ajaxTransaction(param)
            .then(function (result) {
                console.log(JSON.stringify(result));
                $("#content").text(JSON.stringify(result));
            }, function (error) {
                alert('오류발생');
            });
    }

    indexPage.getData();
});