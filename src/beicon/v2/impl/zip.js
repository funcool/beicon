import { shiftResultSelector, isFunction } from "./util.js";
import op from "rxjs";

export function zip(...sources) {
  const projectFunction = shiftResultSelector(sources);

  if (isFunction(projectFunction)) {
    return op.zip(...sources, projectFunction);
  } else {
    return op.zip(...sources);
  }
}
