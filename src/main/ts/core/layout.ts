export type Layout = "wide" | "medium" | "portrait" | "landscape";

export interface Size {
  width: number;
  height: number;
}

const WIDE = 900;
const MEDIUM = 600;
/** Below this height a wide container is a phone on its side: there is no room to stack panels. */
const SHORT = 450;

/** Which arrangement of the panels fits a container (design §3). */
export function layoutFor({ width, height }: Size): Layout {
  if (width > height && height < SHORT && width >= MEDIUM) return "landscape";
  if (width >= WIDE) return "wide";
  if (width >= MEDIUM) return "medium";
  return "portrait";
}
