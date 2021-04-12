function $(expr, con) {
  return typeof expr === 'string'? (con || document).querySelector(expr) : expr;
}

function $$(expr, con) {
  return Array.prototype.slice.call((con || document).querySelectorAll(expr));
}

function xhr(o) {
  var xhreq = new XMLHttpRequest(o.src);

  xhreq.open("GET", o.src);

  xhreq.onreadystatechange = function () {
    if (xhreq.readyState === 4) {
      if (xhreq.status < 400) {
        try {
          o.onsuccess.call(xhreq);
        }
        catch (e) {
          o.onerror.call(xhreq, e);
        }
      }
      else {
        o.onerror.call(xhreq);
      }
    }
  };

  xhreq.send();
}

(function(){

  xhr({
    src: 'latest.json',
    onsuccess: function () {
      var latest = JSON.parse(this.responseText);
      if (latest.filename) {
        $( 'section.lookup > h1').innerHTML = 'Latest development build ' + latest.version;
        $('section.lookup > p').innerHTML = 'Downloading <a href="' + latest.filename + '">' + latest.filename + '</a>';
        location.href = latest.filename;
      }
      else {
        var element = $('section.lookup > p')
        element.innerHTML = 'Lookup failed, no information about latest development build…';
        element.className =  "warning";
      }
    },
    onerror: function (xhreq, e) {
      if (e) {
        console.log(e);
      } else {
        var element = $('section.lookup > p')
        element.innerHTML = 'Lookup failed, no information about latest development build…';
        element.className =  "warning";
      }
    }
  });

})();