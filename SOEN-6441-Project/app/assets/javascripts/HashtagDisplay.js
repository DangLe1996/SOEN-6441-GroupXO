

ws = new WebSocket("ws://" + location.host + "/HashTagWs")

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const product = urlParams.get('hashTag')

ws.onopen = () => ws.send(JSON.stringify({queryTerm: product}));

ws.onmessage = function(event){

    deleterow(tableID);
    addRow(tableID,event.data);

}
const tableID = "TweetTable";


function addRow(tableID, message) {

    const dataToAdd = JSON.parse(message);

    // Get a reference to the table
    let tableRef = document.getElementById(dataToAdd.queryTerm);


    // Insert a row at the end of the table
    let newRow = tableRef.insertRow(2);

    newRow.innerHTML = dataToAdd.htmlCode;

}


function deleterow(tableID) {
    var table = document.getElementById(tableID);
    var rowCount = table.rows.length;

    table.deleteRow(rowCount -1);
}