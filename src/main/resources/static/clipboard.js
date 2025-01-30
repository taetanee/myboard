var clipboardPage = $.extend({}, Common);

clipboardPage.init = function () {
    clipboardPage.eventBind('clipboardPage');
}

clipboardPage.events = {
    'click #btn-success': 'clipboardPage.location.alert'
}

clipboardPage.location.alert = function (){
    alert('1');
}


$(document).ready(function () {
    clipboardPage.init();
});