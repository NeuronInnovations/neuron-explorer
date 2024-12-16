function deleteDevice(name) {
    if (confirm("Do you want delete device?") === true) {
        console.log('delete');
        fetch('delete/'+name.trim(),
            {
                method: "DELETE",
                body: JSON.stringify({}),
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            });
        window.location.href = '/devicemanager';
    } else {
        console.log('cancel');
    }
}

function copyToClipboard(name) {
    var copyText = document.getElementById(name);
    copyText.select();
    document.execCommand("copy");
    M.toast({html: name.charAt(0).toUpperCase()+name.replace("-", " ").slice(1) + ' copied to clipboard!'});
}

function registerDeviceFinal(name) {
    document.getElementById("createdevicefinalandwaitbutton").style.display = "none";   
    document.getElementById("loadingAnimation").style.display = "block";

    fetch('registerfinal/'+name.trim(),
        {
            method: "POST",
            body: JSON.stringify({}),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(json => window.location.href = '/devicemanager');
}