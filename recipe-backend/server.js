// server.js
const express = require("express");
const dotenv = require("dotenv");
const googleSearch = require("./services/googleSearch");
const parseRecipe = require("./services/recipeParser");
const { buildRecipeQuery } = require("./services/recipeSearchService");

dotenv.config();
const app = express();

app.use(express.json());

// POST /search-recipe
app.post("/search-recipe", async (req, res) => {
  try {
    const { ingredients, country, style } = req.body || {};
    
    if (!ingredients || !Array.isArray(ingredients) || !country || !style) {
      return res.status(400).json({ message: "Need ingredients[], country, style" });
    }

    // 1. build query
    const query = buildRecipeQuery(ingredients, country, style);

    // 2. search google
    const links = await googleSearch(query);

    if (!links.length) {
      return res.status(404).json({ message: "No recipe links found" });
    }

    // 3. parse link
    const recipes = await Promise.all(
      links.map((url) => parseRecipe(url, ingredients, style))
    );

    // 4. remove null, sort theo score
    const validRecipes = recipes.filter((r) => r !== null);
    validRecipes.sort((a, b) => b.score - a.score);

    return res.json({
      count: validRecipes.length,
      recipes: validRecipes,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
