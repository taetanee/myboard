const CommonObject = {
    init : function () {}
    , variable : {}
    , events : {}
    , event : {}
    , tran : {}
    , location : {}
    , toast : function (message, type = 'success', removeTime = 3000) {
        // 토스트 컨테이너가 없으면 생성
        if ($('#commonObjectToastContainer').length === 0) {
            $('body').append(`
            <div id="commonObjectToastContainer" style="
                position: fixed;
                bottom: 20px;
                right: 20px;
                z-index: 9999;
                display: flex;
                flex-direction: column;
                gap: 10px;
                align-items: flex-end;
            "></div>
        `);
        }

        // 개별 토스트 요소 생성
        const toastId = 'toast_' + Date.now();
        const $toast = $(`
        <div id="${toastId}" class="alert alert-${type}" style="
            min-width: 200px;
            max-width: 300px;
            box-shadow: 0 0 10px rgba(0,0,0,0.2);
            opacity: 0;
            transform: translateX(100%);
            transition: all 0.3s ease;
        ">
            ${message}
        </div>
        `);

        // 컨테이너에 추가
        $('#commonObjectToastContainer').append($toast);

        // 애니메이션으로 슬라이드 인
        setTimeout(() => {
            $toast.css({
                transform: 'translateX(0)',
                opacity: 1
            });
        }, 10);

        // 3초 뒤 사라짐
        setTimeout(() => {
            $toast.css({
                transform: 'translateX(100%)',
                opacity: 0
            });
            setTimeout(() => $toast.remove(), 300);
        }, removeTime);
    }
    , ajax: function (_param) {
        return new Promise(function (resolve, reject) {
            if( CommonObject.isNull(_param.url) ) {
                CommonObject.error("url is null");
                return;
            }

            const method = _param.type ? _param.type.toUpperCase() : 'POST';

            $.ajax({
                url             : _param.url
                , type          : method
                , headers       : {
                    "X-KAPI-Request" : $("#xKapiRequest").val()
                }
                , dataType      : "json"
                , data          : method === 'GET' ? _param.data : JSON.stringify(_param.data)
                , contentType   : method === 'GET' ? undefined : 'application/json; charset=utf-8'
                , async         : true
                , beforeSend    : function (xhr) {
                    CommonObject.showLoading();
                }
                , success       : function(response) {
                    CommonObject.hideLoading();
                    switch (response.statusCode) {
                        case 200:
                            break;
                        default:
                            if (!CommonObject.isNull(response.detailMessage)) {
                                alert(response.detailMessage);
                            }
                            if (!CommonObject.isNull(response.resultMessage)) {
                                alert(response.resultMessage);
                            }
                            break;
                    }
                    resolve(response);
                }
                , error         : function(xhr, errorName, error) {
                    CommonObject.hideLoading();
                    switch (xhr.status) {
                        case 400:
                            CommonObject.error("ajax 요청중 400 에러가 발생했습니다");
                            break;
                        case 401:
                            CommonObject.error("ajax 요청중 401 에러가 발생했습니다");
                            break;
                        case 403:
                            CommonObject.error("ajax 요청중 403 에러가 발생했습니다");
                            break;
                        case 404:
                            CommonObject.error("ajax 요청중 404 에러가 발생했습니다");
                            break;
                        case 500:
                            CommonObject.error("ajax 요청중 500 에러가 발생했습니다");
                            break;
                        default:
                            CommonObject.error("ajax 요청중 " + xhr.status + " 에러가 발생했습니다");
                            break;
                    }
                    reject(xhr);
                }
                , complete: function () {
                    CommonObject.hideLoading();
                }
            });

        });
    }
    , eventBind : function(screenObj){
        var screenId = screenObj;
        if(typeof screenId == 'string'){
            screenObj = window[screenId];
        }

        //[시작] event 바인딩
        if ( screenObj.events ){
            var keyList = Object.keys(screenObj.events);

            $(keyList).each(function(idx, key){
                var keys = $.trim(key);
                var pos = keys.indexOf(' ');
                var evt = keys.substring(0, pos);
                var selecter = keys.substring(pos+1);
                var func = screenObj.events[keyList[idx]];
                var nameList = func.split('.').slice(1);
                var targetFunc = screenObj;
                nameList.forEach(function(name, idx) {
                    targetFunc = targetFunc[name];
                });
                $('body').off(evt, selecter).on(evt, selecter, targetFunc);
            });
        }
        //[종료] event 바인딩
    }
    , drawInitPage : function(screenObj){
        //[시작] required에 따라 html에 화면 그리기
        screenObj.find('[required]').each(function() {
            const html = '<span class="text-red">*</span>';

            const label = $(this)
                .closest('.form-group')
                .find('label');

            if (label.find('span.text-red').length === 0) {
                label.append(html);
            }
        });
        //[종료] required에 따라 html에 화면 그리기
    }
    , isNull :  function (_param) {
        if( _param === undefined || _param === null || _param === ""){
            return true;
        } else {
            return false;
        }
    }
    , error : function (_msg, isThrowMode){
        alert(_msg);
        if( isThrowMode === undefined && isThrowMode === true){
            throw (_msg);
        }

    }
    , clone : function (){
        const me = this;
        return $.extend(true, {}, me);
    }
    , makeInputData(oSearchArea, inputData, prefix, nullOk, sObj) {
        if( CommonObject.isNull(inputData) ){
            inputData = new Object();
        }

        if(CommonObject.isNull(sObj)){
            sObj = $('#f-content');
        }

        // 검색 시작날짜, 종료날짜 설정
        // oSearchArea.find('input[name="datetimes"]').each(function(i){
        //     var preFixId = $(this).attr('id');
        //     $('#'+preFixId+'_S_DT').val($(this).getStartDate());
        //     $('#'+preFixId+'_E_DT').val($(this).getEndDate());
        // });

        // 시작시간, 종료시간 설정
        // oSearchArea.find('div[name="timepicker"]').each(function(i){
        //     var preFixId = $(this).attr('id');
        //     $('#'+preFixId+'_S_DTM').val($(this).getStartRange());
        //     $('#'+preFixId+'_E_DTM').val($(this).getEndRange());
        // });

        inputData = $.extend(inputData, oSearchArea.fn_getInputdata(prefix, nullOk));

        // oSearchArea.find('input:text[numberOnly]').each(function(i){
        //     inputData[$(this).attr('id')] = BoAlarmCommon.number.removeCommmas($(this).val());
        // });

        // 검색영역 외에 추가되어야 할 항목들.
        // if($('#pageRowCnt', sObj).length > 0){
        //     inputData["pageRowCnt"] = $('#pageRowCnt', sObj).val();
        // }
        //-- 검색영역 외에 추가되어야 할 항목들.

        return inputData;
    }
    , NVL : function (sParam, sDefalut){
        if(CommonObject.isNull(sParam))
            return sDefalut;
        else
            return sParam;
    }
    , validationCheck(objSection){
        var result = true;
        var inputObj = null;
        var msg = "";

        if(CommonObject.isNull(objSection)){
            inputObj = $('[required]');
        }
        else{
            inputObj = $('[required]', objSection);
        }


        /*필수값 체크 s*/
        $(inputObj).each(function(){
            var value = $(this).val();
            //var title = jQuery("label[for='"+$(this).attr("id")+"']").text();
            //var title = $(this).prev("label").text().trim().replace('*','');
            var title = $(this).closest('.form-group').find('label').text().trim().replace('*','');

            if(CommonObject.isNull(title)){
                title = $(this).attr("title");
            }
            if(CommonObject.isNull(title)){
                title = $(this).attr("placeholder");
            }
            if(CommonObject.isNull(title)){
                title = $(this).closest('td').prev('th').text().trim().replace('*','');
            }
            if(CommonObject.isNull(title)){
                title = $(this).closest('div').prev('div').text().trim().replace('*','');
            }

            if(CommonObject.isNull(value)){
                result = false;
                msg = CommonObject.NVL(msg, title+"는(은) 필수 입력입니다.");
            }
        });


        if(!result){
            alert(msg);
            return result;
        }
        /*필수값 체크 e*/

        return result;
    }
    , displayNoData : function (_target){
        let colSpanCnt = _target.closest("table").find("th").length;
        _target.html('<td style="text-align: center" colspan="'+colSpanCnt+'">데이터가 존재하지 않습니다.</td>')
    }
    , displayInit: function (_target) {
        _target.html("");
        _target.closest("table").siblings("div").html("");
    }
    , display : function ( _target, _dummy, result ){
        if (CommonObject.isNull(result) || CommonObject.isNull(result.data) || result.data.length === 0) {
            CommonObject.displayNoData(_target);
            return;
        }
        const _template = _dummy.html();
        const template = Handlebars.compile(_template);
        result.data.forEach(function (item, idx) {
            const html = template(item);
            _target.append(html);
        });
    }
    , setPaging: function (_target, sendData, result, currentPageHandler, clickHandler) {

        const current = CommonObject.NVL(sendData.currentPage, 1);
        const startPage = 1;
        const endPage = Math.ceil(result[0].totalCount / CommonObject.NVL(sendData.rowCount, 20));
        const totalPage = endPage;

        let pageHtml = "";

        if (0 <= startPage && startPage <= current && current <= endPage && endPage <= totalPage) {
            pageHtml += '<div class="dataTables_paginate paging_simple_numbers">';
            pageHtml += '<ul class="pagination">';

            // 이전 버튼 추가
            pageHtml += '<li class="paginate_button previous"><a href="javascript:' + currentPageHandler + '=' + CommonObject.getBeforePage(current) + ';' + clickHandler + '(' + CommonObject.getBeforePage(current) + ');">이전</a></li>';

            for (let i = startPage; i <= endPage; i++) {
                if (i === 1 || i === totalPage || (i >= current - 1 && i <= current + 1)) {
                    if (i === current) {
                        pageHtml += '<li class="paginate_button active"><a href="javascript:void(0)">' + i + '</a></li>';
                    } else {
                        pageHtml += '<li class="paginate_button"><a href="javascript:' + currentPageHandler + '=' + i + ';' + clickHandler + '(' + i + ')">' + i + '</a></li>';
                    }
                } else if (i === current - 2 || i === current + 2) {
                    pageHtml += '<li class="paginate_button disabled"><a href="javascript:void(0)">…</a></li>';
                }
            }

            // 다음 버튼 추가
            pageHtml += '<li class="paginate_button next"><a href="javascript:' + currentPageHandler + '=' + CommonObject.getNextPage(current, totalPage) + ';' + clickHandler + '(' + CommonObject.getNextPage(current, totalPage) + ')">다음</a></li>';
            pageHtml += '</ul>';
            pageHtml += '</div>';

            // 수정된 페이징 삽입 방식 (기존 방식은 전체 문서에서 첫 번째 페이징을 찾음)
            const $table = _target.closest("table");
            const $existing = $table.next(".dataTables_paginate");

            if ($existing.length === 0) {
                $table.after(pageHtml);
            } else {
                $existing.replaceWith(pageHtml);
            }
        }
    }
    , getBeforePage : function (current) {
        if (current - 1 < 1) {
            return 1;
        } else {
            return current - 1;
        }
    }
    , getNextPage : function (current, totalPage) {
        if (current + 1 <= totalPage) {
            return current + 1;
        } else {
            return current;
        }
    }
    , openModal: function (url, param) {
        fetch(url)
            .then(response => response.text())
            .then(html => {
                const $modal = $("#modal-common");
                $modal.html(html);

                //[시작] 파라미터를 input hidden으로 전달
                if (param && Object.keys(param).length > 0) {
                    Object.entries(param).forEach(([key, value]) => {
                        $modal.append(
                            `<input type="hidden" id="${key}" value="${value}">`
                        );
                    });
                }
                //[종료] 파라미터를 input hidden으로 전달

                $modal.modal('show');
            })
            .catch(err => CommonObject.error('팝업 로딩 실패:' + err));
    }
    , setDetails : function (oJsonObj, pArea) {
        var objs = null;
        if(this.isNull(pArea))
            pArea = $('html');

        if(oJsonObj == null){
            return;
        }

        objs = pArea.find("input");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].name;
//		var skey = !gfn_isNull(objs[i].id) ? objs[i].id : objs[i].name;


            if(skey != null && skey != ''){
                try{
                    if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){
                        var svalue = eval("oJsonObj." + skey);
                        if(svalue == "null" || svalue == "[object Object]"){
                            continue;
                        }
                        if(objs[i].type == 'radio'){
                            $(':input:radio[name="'+objs[i].name+'"]:input[value="'+svalue+'"]').attr("checked", true);
                        }
                        else if(objs[i].type == 'checkbox'){
                            var checkArr = svalue.split(",");
                            for(var k= 0; k<checkArr.length ; k++){
                                $(':input:checkbox[name="'+objs[i].name+'"]:input[value="'+checkArr[k]+'"]').attr("checked", true);
                            }
                        }
                        else{
                            objs[i].value = svalue;
                        }

                        if($('#' + skey).attr("isNum") == "Y"){
                            //$('#' + skey).toPrice();
                            $('#' + skey).val(gfn_maskAmt(svalue, true));
                        }

                        if($('#' + skey).attr("isDate") == "Y"){
                            $('#' + skey).val(gfn_maskDate(svalue));
                        }
                    }
                } catch (e) {

                }
            }
        }

        objs = pArea.find("textarea, p");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            if(skey != null && skey != ''){
                if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){

                    var svalue = eval("oJsonObj." + skey);
                    if(svalue == "null" || svalue == "[object Object]"){
                        continue;
                    }
                    objs[i].value = svalue;
                }
            }
        }

        objs = pArea.find("select");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            if(skey != null && skey != ''){
                if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){

                    var svalue = eval("oJsonObj." + skey);
                    if(svalue == "null" || svalue == "[object Object]"){
                        continue;
                    }
                    objs[i].value = svalue;
                }
            }
        }

        objs = pArea.find("span, em");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            if(skey != null && skey != ''){
                try {
                    objs[i].innerHTML = oJsonObj[skey];
                } catch (e) {

                }
            }
        }

        objs = pArea.find("img");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            var altKey = objs[i].id + "_ALT";

            if((skey != null && skey != '')){
                try{
                    var svalue = "";
                    var altValue = "IMAGE";

                    if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){
                        svalue = eval("oJsonObj." + skey);
                        if(svalue == "null" || svalue == "[object Object]"){
                            continue;
                        }
                        objs[i].src = svalue;
                    }

                    if(eval("oJsonObj." + altKey) || eval("oJsonObj." + altKey) == 0 ){
                        altValue = eval("oJsonObj." + altKey);
                        objs[i].alt = altValue;
                    } else {
                        objs[i].alt = altValue;
                    }
                } catch (e) {

                }
            }
        }

        objs = pArea.find("td,th,p");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            if(skey != null && skey != ''){
                if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){

                    var svalue = eval("oJsonObj." + skey);
                    if(svalue == "null" || svalue == "[object Object]"){
                        continue;
                    }

                    if($('#' + skey).attr("isNum") == "Y"){
                        svalue = gfn_maskAmt(svalue);
                    }
                    if($('#' + skey).attr("isDate") == "Y"){
                        svalue =  gfn_maskDate(svalue);
                    }

                    objs[i].innerHTML = svalue;
                }
            }
        }

        objs = pArea.find("a");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            if(skey != null && skey != ''){
                try{
                    if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){

                        var svalue = eval("oJsonObj." + skey);
                        if(svalue == "null" || svalue == "[object Object]"){
                            continue;
                        }
                        $(objs[i]).attr("href", svalue);
                    }
                } catch(e) {

                }
            }
        }

    }
    , getUrlParams: function () {
        const params = new URLSearchParams(window.location.search);
        const result = {};
        for (const [key, value] of params.entries()) {
            result[key] = value;
        }
        return result;
    }
    , showLoading: function () {
        const $content = $('section.content');
        if ($content.find('#loadingLayer').length) return;

        const layer = `
      <div id="loadingLayer" style="
          position:fixed;
          top:50%; left:50%;
          transform:translate(-50%,-50%);
          z-index:2000;
          display:flex; flex-direction:column;
          align-items:center; justify-content:center;
          font-family:'Noto Sans KR',sans-serif;">
        <div class="spinner"></div>
        <div class="loading-text">처리중...</div>
      </div>
      <style>
        .spinner {
          width:36px;height:36px;
          border:4px solid rgba(0,0,0,0.1);
          border-top-color:#28a745;
          border-radius:50%;
          animation:spin 1s linear infinite;
        }
        .loading-text {
          margin-top:14px;padding:6px 14px;
          background:#fff;border-radius:6px;
          color:#28a745;
          font-weight:600;
          box-shadow:0 0 6px rgba(0,0,0,0.15);
        }
        @keyframes spin { to { transform:rotate(360deg); } }
      </style>
    `;

        $content.css('position', 'relative').append(layer);
    }
    , hideLoading: function () {
        $('#loadingLayer').fadeOut(200, function(){ $(this).remove(); });
    }

    , fileUpload: function (obj) {
        const fileInput = document.getElementById(obj.fileInputId || "fileInput");
        const fileList = document.getElementById(obj.fileListId || "fileList");
        const uploadUrl = obj.uploadUrl || "/upload";

        if (fileInput.files.length === 0) {
            alert("파일을 선택하세요.");
            return;
        }

        const file = fileInput.files[0];
        const listItem = document.createElement("li");
        listItem.textContent = `업로드된 파일: ${file.name} (${(file.size / 1024).toFixed(2)} KB)`;
        fileList.appendChild(listItem);

        // 파일 업로드를 서버로 전송하는 코드 (예제)
        const formData = new FormData();
        formData.append(obj.fileParamName || "file", file);

        fetch(uploadUrl, {
            method: "POST",
            body: formData
        })
            .then(response => response.json())
            .then(data => alert("파일 업로드 성공!"))
            .catch(error => alert("파일 업로드 실패: " + error));
    }
    , fileDownload : function (obj) {
        if (!obj.fileName) {
            alert("파일명이 필요합니다.");
            return;
        }

        const link = document.createElement("a");
        link.href = `${obj.downloadUrl || "/download"}?fileName=${encodeURIComponent(obj.fileName)}`;
        link.setAttribute("download", obj.fileName);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

}


$.fn.fn_getInputdata = function(prefix, nullOk){
    prefix = CommonObject.NVL(prefix, "");
    nullOk = CommonObject.NVL(nullOk, false);
    var data = {};
    var setVal = function(obj){
        //var id = (gfn_isNull(obj.attr('name')))? obj.attr('id'):obj.attr('name');
        var id = obj.attr('id');
        var name = obj.attr('name');
        if(obj.attr('type') == 'radio'){
            id = (CommonObject.isNull(obj.attr('name')))? obj.attr('id'):obj.attr('name');
        }
        var val = obj.val();

        if(nullOk){
            if(!CommonObject.isNull(id)){
                if(obj.attr('type') == 'checkbox'){
                    val = ($(obj).attr('checked')) ? "Y" : "N";
                    //data[prefix + name] = $.extend({(prefix + id) : val}, data[prefix + name]);
                    var tData = {};
                    var cd = CommonObject.string.replaceAll(id, name+'_', '');
                    tData[cd] = val;
                    data[prefix + name] = $.extend(tData, CommonObject.NVL(data[prefix + name], {}));
                    if("Y" == val){
                        if(CommonObject.isNull(data[prefix + name].CHECKLIST)){
                            data[prefix + name]['CHECKLIST'] = new Array();
                        }
                        data[prefix + name].CHECKLIST.push(cd);
                    }
                }
                else{
                    data[prefix + id] = CommonObject.NVL(val, "");
                }
            }
        }
        else{
            if(!CommonObject.isNull(val) && !CommonObject.isNull(id)){
                if(obj.attr('type') == 'checkbox'){
                    val = ($(obj).attr('checked')) ? "Y" : "N";
                    var tData = {};
                    var cd = CommonObject.string.replaceAll(id, name+'_', '');
                    tData[cd] = val;
                    data[prefix + name] = $.extend(tData, CommonObject.NVL(data[prefix + name], {}));
                    if("Y" == val){
                        if(CommonObject.isNull(data[prefix + name].CHECKLIST)){
                            data[prefix + name]['CHECKLIST'] = new Array();
                        }
                        data[prefix + name].CHECKLIST.push(cd);
                    }
                }
                else{
                    data[prefix + id] = val;
                }
            }
            else if(CommonObject.isNull(val) && !CommonObject.isNull(id)){
                data[prefix + id] = '';
            }
        }
    };

    $(this).find('input,select,combo,textarea').each(function(i){
//		if($(this).attr('type') == 'checkbox' || $(this).attr('type') == 'radio'){
        if($(this).attr('type') == 'radio'){
            if($(this).attr('checked')){
                setVal($(this));
            }
        }else{
            setVal($(this));
        }
    });

    return data;
}