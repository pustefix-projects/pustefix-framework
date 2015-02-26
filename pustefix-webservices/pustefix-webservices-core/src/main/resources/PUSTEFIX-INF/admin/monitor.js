function toggleDetails(src,id) {
  var elems=document.getElementsByTagName('tr');
  for(var i=0;i<elems.length;i++) {
    if(elems[i].className=='sel') {
      elems[i].className='nosel';
    }
  }
  src.className='sel';
  elems=document.getElementsByTagName('div');
  for(var i=0;i<elems.length;i++) {
    if(elems[i].className=='detailentry') {
      if(elems[i].id==id) {
        elems[i].style.display='block';
      } else {
        elems[i].style.display='none';
      }
    }
  }
}