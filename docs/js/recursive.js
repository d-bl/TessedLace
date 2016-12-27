var data = [0,1,2,3,4]
function getRadioVal(radioName) {
  var rads = document.getElementsByName(radioName);

  for(var i=0; i<rads.length; i++) {
    if(rads[i].checked)
      return rads[i].value;
  }
  return null;
}
function setMatrix(value) {
  var p = unescape(value).split(";")
  if (p.length == 6) {
      document.getElementById("matrix").value = p[0]
      document.getElementById("tiles").value = p[1]
      document.getElementById("rows").value = p[2]
      document.getElementById("cols").value = p[3]
      document.getElementById("shiftLeft").value = p[4]
      document.getElementById("shiftUp").value = p[5]
  }
}
function pairDiagram(value) {
  var matrix = document.getElementById("matrix").value
  var tiling = document.getElementById("tiles").value
  var nrOfRows = document.getElementById("rows").value
  var nrOfCols = document.getElementById("cols").value
  var shiftLeft = document.getElementById("shiftLeft").value
  var shiftUp = document.getElementById("shiftUp").value

  var stitch = document.getElementById("s1").value
  data[1] = dibl.D3Data().get(matrix, nrOfRows, nrOfCols, shiftLeft, shiftUp, stitch, tiling)
  document.getElementById('d0').innerHTML = ""
  document.getElementById('d1').innerHTML = ""
  diagram.showGraph({
    container: '#d0',
    nodes: data[1].pairNodes(),
    links: data[1].pairLinks(),
    viewWidth: 360,
    viewHeight: 160
  })
  diagram.showGraph({
    container: '#d1',
    nodes: data[1].threadNodes(),
    links: data[1].threadLinks(),
    threadColor: '#color',
    viewWidth: 360,
    viewHeight: 160
  })
}
function threadDiagram(n) {
  var stitch = document.getElementById("s" + n).value
  data[n] = dibl.D3Data().get(stitch, data[n-1])
  document.getElementById('d'+n).innerHTML = ""
  diagram.showGraph({
    container: '#d' + n,
    nodes: data[n].threadNodes(),
    links: data[n].threadLinks(),
    threadColor: '#color',
    viewWidth: 660,
    viewHeight: 300
  })
}