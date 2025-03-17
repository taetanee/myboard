/**
 * Common 객체
 */
var CommonObject = {
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
    , NVL : function (sParam, sDefalut){
        if(CommonObject.isNull(sParam))
            return sDefalut;
        else
            return sParam;
    }
    , error: function (msg) {
        alert(msg);
        throw(msg);
    }
    , getQueryParam(key) {
        const params = new URLSearchParams(window.location.search);
        return params.get(key);
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
            if (CommonObject.isNull(_param)) {
                CommonObject.error("param is null");
            }

            if (CommonObject.isNull(_param.url)) {
                CommonObject.error('url is null');
            }

            var reqUrl = _param.url

            $.ajax({
                url: reqUrl
                , type: "POST"
                , async: false
                , data          : JSON.stringify(_param.data)
                , contentType   : 'application/json; charset=utf-8'
                , success: function (resData) {
                    resolve(resData);
                }
                , error: function (xhr, errorName, error) {
                    reject(xhr);
                }
            });
        });
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
//						svalue = eval("oJsonObj." + skey);
//						skey = !gfn_isNull(objs[i].name) ? objs[i].name : objs[i].id;
                            $(':input:radio[name="'+objs[i].name+'"]:input[value="'+svalue+'"]').attr("checked", true);
                        }
                        else if(objs[i].type == 'checkbox'){
//						svalue = eval("oJsonObj." + skey);
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
                }
                catch(e){}
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
                    // original
                    objs[i].value = svalue;
                    // css 특수
                    //gfn_setSelect($("#"+skey), svalue);
                }
            }
        }

        objs = pArea.find("span, em");
        for(var i = 0; i< objs.length; i++){
            var skey = objs[i].id;
            /*
            if(skey == "payment_distribute_expl"){
                debugger;
            }
            */
            if(skey != null && skey != ''){
                try{
                    if(eval("oJsonObj." + skey) || eval("oJsonObj." + skey) == 0 ){

                        var svalue = eval("oJsonObj." + skey);
                        if(svalue == "null" || svalue == "[object Object]"){
                            continue;
                        }
                        //gfn_log(skey + " || " + skey.lastIndexOf("_dis"));
                        if(skey.endsWith("_dis")) {
                            // 숫자의 경우 양수는 빨간색,  음수는 파랑색 표시
                            if(ComUtil.null($(objs[i]).data("addclass"), false) == true){
                                if( parseFloat(svalue) > 0 ){
                                    $(objs[i]).addClass('plus');
                                }
                                else if( parseFloat(svalue) < 0 ){
                                    $(objs[i]).addClass('minus');
                                }
                            }
                            // 내려온 유닛정보를 표시할지 여부
                            /*if(ComUtil.null($(objs[i]).data("nounit"), false) == false){
                                unitKey = skey.substr(0, skey.lastIndexOf("_dis")) + '_unit';
                                //console.log(unitKey);
                                svalue = svalue + ComUtil.null($(objs[i]).data("space"), "") + eval("oJsonObj." + unitKey);
                            }
                            else{
                                // unit 이 분리된경우에  빨간색, 파랑색 적용이 필요할 경우
                                unitKey = skey.substr(0, skey.lastIndexOf("_dis")) + '_unit';
                                if(ComUtil.null($(objs[i]).data("addclass"), false) == true){
                                    if( parseFloat(svalue) > 0 ){
                                        $('#'+unitKey, pArea).addClass('plus');
                                    }
                                    else if( parseFloat(svalue) < 0 ){
                                        $('#'+unitKey, pArea).addClass('minus');
                                    }
                                }
                            }*/

                            // 플러스의 경우 앞에 + 표시를 할지 여부 (안쓰를걸로함.)
                            /*
                            if(ComUtil.isNull($(objs[i]).data("addplus")) == false){
                                valueKey = skey.substr(0, skey.lastIndexOf("_dis")) + '_' + $(objs[i]).data("addplus");
                                if( parseFloat(eval("oJsonObj." + valueKey)) > 0 ){
                                    svalue = "+" + svalue;
                                }
                            }
                            */
                        }
                        if($('#' + skey).attr("isNum") == "Y"){
                            svalue = gfn_maskAmt(svalue, true);
                        }
                        if($('#' + skey).attr("isDate") == "Y"){
                            svalue =  gfn_maskDate(svalue);
                        }
                        objs[i].innerHTML = svalue;
                    }
                }catch(e){}
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
                }catch(e){}
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
                }catch(e){}
            }
        }


        // footer 호출
        try{changeContentSize();}catch(e){}
    }
};


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