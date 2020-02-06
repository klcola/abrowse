// 切换国际化语言
function changeLang (lang) {
  sessionStorage.setItem('lang', lang)
  window.location.href = '?lang=' + lang
  if (lang === 'zh_CN') {
    $('#languageSpan')[0].innnerText = '中文'
  } else {
    $('#languageSpan')[0].innnerText = 'English'
  }
}
//	侧边栏跳转链接 添加 国际化请求参数
function linkChange (url) {
  if (sessionStorage.getItem('lang')) {
    window.location.href = url + '?lang=' + sessionStorage.getItem('lang')
  } else {
    window.location.href = url + '?lang=' + 'zh_CN'
  }
}
