var indexPage = $.extend({}, Common);

indexPage.init = function () {

    indexPage.location.getShortWeather();

    indexPage.location.getCovid();
}

indexPage.location.getCurrentPosition = function () {
    navigator.geolocation.getCurrentPosition(function (pos) {
        var result = {
            latitude: pos.coords.latitude
            , longitude: pos.coords.longitude
        }
        return result;
    });
}

indexPage.location.getCovid = function () {

    var param = {
        url: "/covid/getCovid"
    }
    indexPage.ajaxTransaction(param).then(function (result) {
        $("#covid").text(JSON.stringify(result));
    });
}

indexPage.location.getShortWeather = function () {

    var currentPosition = indexPage.location.getCurrentPosition();
    if (currentPosition == undefined) {
        alert('위치정보를 허용하지 않은 상태');
        currentPosition = {
            nx : null
            , ny : null
        }
    }

    var param = {
        url: "/weather/getShortWeather"
        , nx: currentPosition.latitude
        , ny: currentPosition.longitude
    }

    indexPage.ajaxTransaction(param).then(function (result) {
        var item = result.result
        item.forEach((item, index) => {
            switch (item.category) {
                case "T1H":
                    item.categoryNm = "기온 ℃";
                    break;
                case "RN1":
                    item.categoryNm = "1시간 강수량 mm";
                    break;
                case "UUU":
                    item.categoryNm = "동서바람성분 m/s";
                    break;
                case "VVV":
                    item.categoryNm = "남북바람성분 m/s";
                    break;
                case "REH":
                    item.categoryNm = "습도 %";
                    break;
                case "PTY":
                    item.categoryNm = "강수형태";
                    break;
                case "VEC":
                    item.categoryNm = "풍향 deg";
                    break;
                case "WSD":
                    item.categoryNm = "풍속 m/s";
                    break;
                default:
                    alert('예기치 않은 오류 발생');
                    break;
            }
        });
        $("#short_weather").text(JSON.stringify(item));

    }, function (error) {
        alert('오류발생');
    });
}

$( document ).ready(function() {
    indexPage.init();
});