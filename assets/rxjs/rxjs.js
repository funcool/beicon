import * as rxjs from "rxjs";
import * as operators from "rxjs/operators";

if (typeof self !== "undefined") { init(self); }
else if (typeof global !== "undefined") { init(global); }
else if (typeof window !== "undefined") { init(window); }
else { throw new Error("unsupported execution environment"); }


function init(g) {
  g.rxjs = Object.assign({}, rxjs, {operators});
}

