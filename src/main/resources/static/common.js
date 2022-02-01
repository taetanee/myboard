function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}


/*
* 원금균등상환
* _total_times : 회차
* _this_times : 재귀함수의 회차
* _principal : 원금
* _balance : 잔액(첫달에는 희망대출 금액)
* _interest : 월 이자
* _interest_rate : 이자율
* */
function getRecursiveAnnualAverageInterest(_total_times, _this_times, _principal, _balance, _interest, _interest_rate ) {
    if( _this_times > _total_times){
        return 0;
    } else {
        //이번 달 잔액 = 이전 달 잔액 - 원금
        var balance = _balance - _principal;

        //이번 달 월 이자 = 이전 달 잔액 * 이자 / 12
        var interest = _balance * _interest_rate / 12;

        //console.log(_this_times +" : " + interest);

        return interest + getRecursiveAnnualAverageInterest(_total_times, _this_times+1, _principal, balance, interest, _interest_rate);
    }
}


function getAnnualAverageInterest(_hope_loan, _loan_year, _interest_rate){
    //getRecursiveAnnualAverageInterest(480,1,416667,200000000,500000,0.03) / 40
    var result = getRecursiveAnnualAverageInterest(_loan_year * 12, 1,  _hope_loan / ( _loan_year * 12) , _hope_loan, _hope_loan * _interest_rate / 12 , _interest_rate) / _loan_year;
    return result;
}