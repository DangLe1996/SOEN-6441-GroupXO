

ws = new WebSocket("ws://" + location.host + "/HashTagWs")

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const product = urlParams.get('hashTag')

ws.onopen = () => ws.send(product);

ws.onmessage = function(event){

    deleterow(tableID);
    addRow(tableID,event.data);

}
const tableID = "TweetTable";

ws.on('disconnect', function () {
    ws.send("KillSwitch")
});




function addRow(tableID, textToAdd) {
    // Get a reference to the table
    let tableRef = document.getElementById(tableID);


    // Insert a row at the end of the table
    let newRow = tableRef.insertRow(2);

    newRow.innerHTML = textToAdd;

}


function deleterow(tableID) {
    var table = document.getElementById(tableID);
    var rowCount = table.rows.length;

    table.deleteRow(rowCount -1);
}