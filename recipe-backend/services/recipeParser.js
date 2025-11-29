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
    if (type) {
      if (Array.isArray(type) && type.includes("Recipe")) return data;
      if (type === "Recipe") return data;
    }

    if (data["@graph"]) {
      const r = findRecipeNode(data["@graph"]);
      if (r) return r;
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
