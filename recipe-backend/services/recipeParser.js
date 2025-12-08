const axios = require("axios");
const cheerio = require("cheerio");

module.exports = async function parseRecipe(url, ingredients, style) {
  try {
    const res = await axios.get(url, { timeout: 8000 });
    const $ = cheerio.load(res.data);

    let recipeData = null;

    // script ld+json, check  @type = "Recipe"
    $('script[type="application/ld+json"]').each((_, el) => {
      if (recipeData) return; // đã tìm được rồi thì thôi

      const jsonText = $(el).contents().text();
      try {
        const data = JSON.parse(jsonText.trim());
        const found = findRecipeNode(data);
        if (found) recipeData = found;
      } catch (e) {
        //skip JSON parse error
      }
    });

    if (!recipeData) {
      // can not find recipe data
      return null;
    }

    // get ingredients
    const ing =
      recipeData.recipeIngredient ||
      recipeData.ingredients ||
      [];

    // get steps
    let rawSteps = recipeData.recipeInstructions || [];
    let steps = [];
    if (Array.isArray(rawSteps)) {
    steps = rawSteps
    .map((item) => {
      if (typeof item === "string") return item;
      if (item["@type"] === "HowToStep" && item.text) return item.text;
      if (typeof item.text === "string") return item.text;
      if (typeof item.name === "string") return item.name;
      return null;
    })
    .filter(Boolean);
}
    if (Array.isArray(rawSteps)) {
      steps = rawSteps
        .map((s) => {
          if (typeof s === "string") return s;
          if (typeof s.text === "string") return s.text;
          if (typeof s.name === "string") return s.name;
          return null;
        })
        .filter(Boolean);
    } else if (typeof rawSteps === "string") {
      // if rawSteps is a single string, split by new lines
      steps = rawSteps
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter(Boolean);
    }

    const name =
      recipeData.name ||
      $("h1").first().text().trim() ||
      "Unknown Recipe";

    const score = calcScore(ingredients, ing, style, recipeData);

    return {
      name,
      url,
      score,
      ingredients: ing,
      steps
    };
  } catch (err) {
    // console.error("parseRecipe error for", url, err.message);
    return null;
  }
};

// maybe objsect in (array, @graph, object...)
function findRecipeNode(data) {
  if (!data) return null;

  if (Array.isArray(data)) {
    for (const d of data) {
      const r = findRecipeNode(d);
      if (r) return r;
    }
    return null;
  }

  if (typeof data === "object") {
    const type = data["@type"];

    // 1. @type Recipe
    if (type) {
      if (Array.isArray(type)) {
        if (type.includes("Recipe")) return data;
      } else if (type === "Recipe") {
        return data;
      }
    }

    // 2.  @type addition BlogPosting, Article…  recipeIngredient + recipeInstructions + name/headline
    if (
      data.recipeIngredient &&
      data.recipeInstructions &&
      (data.name || data.headline)
    ) {
      return data;
    }

    // 3. @graph
    if (data["@graph"]) {
      const r = findRecipeNode(data["@graph"]);
      if (r) return r;
    }

    // 4. search in all keys
    for (const key in data) {
      if (Array.isArray(data[key])) {
        const r = findRecipeNode(data[key]);
        if (r) return r;
      }
    }
  }
  return null;
}
function calcScore(userIngredients, recipeIngredients, style, data) {
  const recipeText = recipeIngredients.join(" ").toLowerCase();
  let match = 0;

  for (let i of userIngredients) {
    if (recipeText.includes(i.toLowerCase())) {
      match++;
    }
  }

  const ingredientScore =
    userIngredients.length > 0
      ? match / userIngredients.length
      : 0;

  const desc = (data.description || "").toLowerCase();
  const styleScore =
    style && desc.includes(style.toLowerCase()) ? 1 : 0;

  return ingredientScore * 0.7 + styleScore * 0.3;
}
