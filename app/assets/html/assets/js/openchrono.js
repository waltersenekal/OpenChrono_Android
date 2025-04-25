
var svgPath = "./assets/img/svg/";

function onJsonDataReceived(_id) {
    //showAndroidToast(_id);
    localStorage.setItem("id", _id);
    Android.onJsonDataReceived(_id);
    //alert('Clicked: ${id}');
}

function ShowAndroidToast(toast) {
    Android.showToast(toast);
}

function init(val) {
    var span = document.getElementById('init_val');
    if ('textContent' in span) {
        span.textContent = val;
    } else {
        span.innerText = val;
    }
}

function handleActionWithData(ids, action) {
    const payload = { action: action };

    // Loop through the array of ids and dynamically get their values
    ids.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            payload[id] = element.value; // Add the id and its value to the payload
        }
    });

    // Pass the payload to onJsonDataReceived
    onJsonDataReceived(JSON.stringify(payload));
}

function handleAction(action) {
    const payload = { action: action };

    // Pass the payload to onJsonDataReceived
    onJsonDataReceived(JSON.stringify(payload));
}

document.addEventListener('DOMContentLoaded', () => {
    // Initialize Materialize select dropdowns
    var elems = document.querySelectorAll('select');
    var instances = M.FormSelect.init(elems, {}); // Pass options if needed

    // Add event listener for the chronograph dropdown
    const chronographSelect = document.getElementById('chronograph');
    const friendlyNameInput = document.getElementById('friendlyname');

    if (chronographSelect && friendlyNameInput) {
        chronographSelect.addEventListener('change', () => {
            // Update the friendlyname input with the selected text value
            const selectedText = chronographSelect.options[chronographSelect.selectedIndex].text;
            friendlyNameInput.value = selectedText !== "No devices Found" ? selectedText : "";
        });
    }
}); 
