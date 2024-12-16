function deleteService() {
    if (confirm("Do you want delete service?") === true) {
        console.log('delete');
    } else {
        console.log('cancel');
    }
}