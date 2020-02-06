//  控制 index.html 侧边栏 显示 / 隐藏
function hideTool () {
  let sidebarWidth = $("#statuspanel").width()
  window.sidebarWidth = sidebarWidth	// 将 sidebarWidth 赋值给 window 对象，为了使得 showTool 方法获取到 sidebarWidth
  $("#statuspanel").hide()
  $("#browser-canvasframe").width($("#browser-canvasframe").width() + sidebarWidth - 1) // border 为 1
  let svgArr = $("svg")
  for (let i = 0;i < svgArr.length;i++) {
    let tempWidth = $(svgArr[i]).width() + sidebarWidth
    $(svgArr[i]).width(tempWidth)
  }
  $("#showIcon").show()
}

function showTool () {
  $("#showIcon").hide()
  $("#statuspanel").show()
  $("#browser-canvasframe").width($("#browser-canvasframe").width() - window.sidebarWidth + 1) // border 为 1
  let svgArr = $("svg")
  for (let i = 0;i < svgArr.length;i++) {
    let tempWidth = $(svgArr[i]).width() - window.sidebarWidth
    $(svgArr[i]).width(tempWidth)
  }
}

//  鼠标悬浮在用户和管理 icon 时显示 tooltip
$(function () {
  $('[data-toggle="tooltip"]').tooltip()
})
