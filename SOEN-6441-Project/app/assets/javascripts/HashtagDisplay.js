

ws = new WebSocket("ws://" + location.host + "/HashTagWs")

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const product = urlParams.get('hashTag')

ws.onopen = () => ws.send(JSON.stringify({queryTerm: product}));

ws.onmessage = function(event){

    deleterow(event.data);
    addRow(event.data);

}

function addRow( message) {

    const dataToAdd = JSON.parse(message);

    // Get a reference to the table
    let tableRef = document.getElementById(dataToAdd.queryTerm);

    // Insert a row at the end of the table
    let newRow = tableRef.insertRow(2);

    newRow.innerHTML = dataToAdd.htmlCode;

}


function deleterow(message) {
    const dataToAdd = JSON.parse(message);

    var table = document.getElementById(dataToAdd.queryTerm);
    var rowCount = table.rows.length;

    table.deleteRow(rowCount -1);
}