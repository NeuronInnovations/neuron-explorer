function addName() {

    name = document.getElementById("device_name_input").value.trim();
    if (name === undefined || name === null || name === '') {
        window.alert("Type a device name")
    } else {
        device.name = document.getElementById("device_name_input").value.trim();

        const request = new XMLHttpRequest();
        request.open("GET", '/api/v1/device/' + device.name, false)
        request.send(null);

        if (request.status === 200) {
            if (request.response) {
                const response = JSON.parse(request.response);
                window.alert('A device named "' + response.name + '" exists. Enter a unique name for your device.');
            }

        }
        if (request.status === 204) {
            document.getElementById("device_name").hidden = true;
            document.getElementById("device_role").hidden = false;
        }
    }
}

function backToName() {
    document.getElementById("device_name").hidden = false;
    document.getElementById("device_role").hidden = true;
    document.getElementById("device_selector").hidden = true;
    document.getElementById("device_name_input").value = device.name;
}

function selectDeviceType(message) {
    document.getElementById("device_role").hidden = true;
    document.getElementById("device_selector").hidden = false;
    if ('CONSUMER' === message) {
        document.getElementById("device_selector_provider").hidden = true;
        document.getElementById("device_selector_consumer").hidden = false;
    }
    if ('PROVIDER' === message) {
        document.getElementById("device_selector_provider").hidden = false;
        document.getElementById("device_selector_consumer").hidden = true;
    }
}

function addDeviceType(message) {
    if ('CONSUMER' === message) {
        device.deviceType = document.getElementById("device_select_consumer").value.trim();
        device.deviceRole = 'CONSUMER'
    }
    if ('PROVIDER' === message) {
        device.deviceType = document.getElementById("device_select_provider").value.trim();
        device.deviceRole = 'PROVIDER'
    }
}

function createDevice() {
    if (device.deviceRole === 'PROVIDER' || device.deviceRole === 'CONSUMER') {
        document.getElementById("createdeviceandwaitbutton").style.display = "none";   
        document.getElementById("loadingAnimation").style.display = "block";

 
        fetch('api/v1/device',
            {
                method: "POST",
                body: JSON.stringify(device),
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            }
        ).then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.details);
                });
            }
            response.json().then(json => window.location.href = '/devicedetails/' + json.name);
        }).catch(error => {
            alert(error)
        });
    } else {
        window.alert("Select a device type")
    }
}

const device = {
    name: "",
    latitude: "",
    longitude: "",
    deviceKey: "",
    deviceRole: "",
    deviceType: ""
}
