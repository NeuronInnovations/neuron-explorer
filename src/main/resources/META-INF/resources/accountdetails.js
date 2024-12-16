function complete() {
    account.firstName = document.getElementById("first-name").value.trim();
    account.lastName = document.getElementById("last-name").value.trim();
    account.hederaAccountNumber = document.getElementById("account-number").value.trim();
    account.publicKey = document.getElementById("public-key").value.trim();
    account.privateKey = document.getElementById("private-key").value.trim();


    document.getElementById("terms_and_conditions").hidden=true;
    document.getElementById("progress-bar").hidden=false;

    fetch('api/v1/account/create',
        {
            method: "POST",
            body: JSON.stringify(account),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }
    ).then(response => location.reload()
    )

}

function copyToClipboard(name) {
    var copyText = document.getElementById(name);
    copyText.select();
    document.execCommand("copy");
    M.toast({html: name.charAt(0).toUpperCase()+name.replace("-", " ").slice(1) + ' copied to clipboard!'});
}

const account = {
    firstName: "",
    lastName: "",
    hederaAccountNumber: "",
    publicKey: "",
    privateKey: "",
}