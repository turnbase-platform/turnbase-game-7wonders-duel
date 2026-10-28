export type Layout = "wide" | "medium" | "portrait" | "landscape";

export interface Size {
  width: number;
  height: number;
}

const WIDE = 900;
const MEDIUM = 600;
/** Below this height a wide container is a phone on its side or a short window: there is no room to stack panels, and the track stands up. */
const SHORT = 640;

/** Which arrangement of the panels fits a container (design §3). */
export function layoutFor({ width, height }: Size): Layout {
  if (width > height && height < SHORT && width >= MEDIUM) return "landscape";
  if (width >= WIDE) return "wide";
  if (width >= MEDIUM) return "medium";
  return "portrait";
}
