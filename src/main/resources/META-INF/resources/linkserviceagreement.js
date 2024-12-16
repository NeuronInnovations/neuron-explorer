function next(message) {
    console.log(message);
    if (message==='agreement') {
        document.getElementById("link_service_agreement").hidden=true;
        document.getElementById("select_service_parameters").hidden=false;
        selectService();
    }
    if (message==='parameters') {
        addAmount(document.getElementById("select_amount").value)
        document.getElementById("select_service_parameters").hidden=true;
        document.getElementById("service-details").hidden=false;
        document.getElementById("service_name").value=serviceRegister.service.name;
        document.getElementById("service_amount").value=serviceRegister.fee;
    }
}

function back(message) {
    if (message==='parameters') {
        document.getElementById("link_service_agreement").hidden=false;
        document.getElementById("select_service_parameters").hidden=true;
    }
    if (message==='details') {
        document.getElementById("link_service_agreement").hidden=false;
        document.getElementById("select_service_parameters").hidden=true;
        document.getElementById("service-details").hidden=true;
    }
}

function selectService() {
    serviceRegister.service.name = document.getElementById("select_service").value;
    console.log(serviceRegister);
}

function addAmount(message) {
    console.log(message);
    console.log('inside addAmount method');
    if (message === undefined || message === null || message === '') {
        window.alert("Type an amount")
    } else {
        serviceRegister.fee = message;
        console.log((serviceRegister));
    }
}

function registerService(message){
    serviceRegister.device.name = message.trim();
    const request = new XMLHttpRequest();
    request.open("POST", '/api/v1/service/register', false)
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(JSON.stringify(serviceRegister));
    window.location.href = '/devicedetails/'+message.trim()
}

const serviceRegister = {
    service: {},
    device:{},
    fee :''
}