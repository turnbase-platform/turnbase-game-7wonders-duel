import { describe, expect, it } from "vitest";
import { withFigures } from "../../main/ts/ui/icons/figures.js";
import { IMPORTED } from "../../main/ts/ui/icons/imported.js";

describe("withFigures", () => {
  const icons = withFigures(IMPORTED);

  it("sets the numbers of the progress tokens as text instead of outlines", () => {
    expect(icons.agriculture.body.match(/<text /g)).toHaveLength(2);
    expect(icons.agriculture.body).not.toContain('d="M43.7 84.4');
    expect(icons.agriculture.body).not.toContain('d="M83.5 76.7');
  });

  it("leaves icons without a number alone", () => {
    expect(icons.law).toBe(IMPORTED.law);
  });
});
