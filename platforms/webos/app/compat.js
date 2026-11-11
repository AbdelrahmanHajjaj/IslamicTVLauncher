/* Small runtime additions required by adhan-js on WebKit 538.2. */
if (!Math.trunc) { Math.trunc = function (n) { return n < 0 ? Math.ceil(n) : Math.floor(n); }; }
if (!Math.sign) { Math.sign = function (n) { n = Number(n); return n === 0 || isNaN(n) ? n : (n > 0 ? 1 : -1); }; }
if (!Number.isNaN) { Number.isNaN = function (n) { return typeof n === 'number' && isNaN(n); }; }
