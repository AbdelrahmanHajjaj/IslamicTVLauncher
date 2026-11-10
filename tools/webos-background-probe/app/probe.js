(function () {
  'use strict';
  var params = {};
  try { params = JSON.parse(window.PalmSystem.launchParams || '{}'); } catch (e) {}
  var bridges = [];
  var audio = document.getElementById('tone');
  var video = document.getElementById('hdmi');
  var result = document.getElementById('result');
  var ticks = 0;
  var closing = false;

  function report(method, data, callback) {
    if (typeof PalmServiceBridge === 'undefined') { return; }
    data.runId = params.probeRunId || null;
    var bridge = new PalmServiceBridge();
    bridges.push(bridge);
    bridge.onservicecallback = function (raw) {
      var index = bridges.indexOf(bridge);
      if (index >= 0) { bridges.splice(index, 1); }
      if (callback) {
        var reply;
        try { reply = JSON.parse(raw); } catch (error) { reply = {}; }
        callback(reply || {});
      }
    };
    bridge.call('luna://tv.afnon.bgprobe.service/' + method, JSON.stringify(data));
  }
  function observe(event, element) {
    var data = {event: event, ticks: ticks};
    if (element) {
      data.currentTime = element.currentTime;
      data.width = element.videoWidth || 0;
      data.height = element.videoHeight || 0;
      data.errorCode = element.error ? element.error.code : 0;
    }
    report('observe', data);
  }
  function close() {
    if (closing) { return; }
    closing = true;
    audio.pause(); video.pause();
    observe('app-closing');
    if (params.mode === 'audio') {
      report('restoreInput', {}, function (reply) {
        if (reply.returnValue) { window.close(); }
        else { result.textContent = 'Input return failed. Use the Input button to choose your source.'; }
      });
    }
    setTimeout(function () { window.close(); }, 300);
  }
  function track(element, prefix) {
    ['loadedmetadata', 'playing', 'ended', 'pause', 'error'].forEach(function (event) {
      if (prefix === 'hdmi' && event === 'ended') { return; }
      element.addEventListener(event, function () {
        observe(prefix + '-' + event, element);
        if (event === 'error') { result.textContent = prefix + ' media error ' + element.error.code; }
      });
    });
  }
  report('visible', {});
  track(audio, 'audio'); track(video, 'hdmi');
  document.addEventListener('webkitvisibilitychange', function () {
    var hidden = document.webkitHidden;
    observe(hidden ? 'app-hidden' : 'app-visible');
    if (hidden) { audio.pause(); video.pause(); }
  });
  document.addEventListener('keydown', function (event) {
    if (event.keyCode === 461 || event.keyCode === 27) { close(); }
  });

  if (params.mode === 'hdmi') {
    result.textContent = 'HDMI 1 inside Afnon. The counter should keep changing.';
    video.style.display = 'block';
    var source = document.createElement('source');
    source.type = 'service/webos-external';
    source.src = 'ext://hdmi:1';
    video.appendChild(source);
    video.load(); video.play();
    setTimeout(function () {
      document.getElementById('banner').style.display = 'block';
      observe('app-banner-shown');
    }, 4000);
    setTimeout(function () {
      document.getElementById('banner').style.display = 'none';
      observe('app-banner-hidden');
    }, 100000);
    setInterval(function () {
      ticks += 1;
      document.getElementById('counter').textContent = ticks + ' seconds';
      if (ticks % 10 === 0 && ticks <= 300) { observe('hdmi-progress', video); }
    }, 1000);
    // shortcut: this is a five-minute probe, not an always-on media shell.
    setTimeout(close, 300000);
  } else if (params.mode === 'audio') {
    result.textContent = 'Background audio reminder. Returning to your input shortly.';
    audio.volume = 0.25;
    audio.src = 'test-tone.mp3';
    audio.load(); audio.play();
    setTimeout(function () { observe('audio-progress', audio); }, 2500);
    setTimeout(function () { if (!audio.ended) { observe('audio-timeout', audio); } close(); }, 12000);
  } else if (params.mode === 'startup') {
    result.textContent = 'Afnon started automatically after a new TV boot. Press Back to return.';
    setTimeout(close, 120000);
  } else if (params.probeRunId) {
    result.textContent = 'Background reminder received at ' + params.firedAt;
  }
}());
