const showcaseImageModules = import.meta.glob(
  "/src/static/funtion_show/*.{png,jpg,jpeg,webp,avif}",
  {
    eager: true,
    import: "default",
  },
);

export const SHOWCASE_IMAGE_URLS = Object.values(showcaseImageModules)
  .filter((value) => typeof value === "string" && value.length > 0)
  .sort();
