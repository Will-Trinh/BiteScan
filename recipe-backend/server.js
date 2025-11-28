
// server.js
const express = require("express");
const dotenv = require("dotenv");
const googleSearch = require("./services/googleSearch");
const parseRecipe = require("./services/recipeParser");
const { buildRecipeQuery } = require("./services/recipeSearchService");
const swaggerUi = require("swagger-ui-express");
const swaggerJsdoc = require("swagger-jsdoc");

dotenv.config();
const app = express();

const swaggerOptions = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Recipe Search API",
      version: "1.0.0",
      description: "API for searching recipes by ingredients and cuisine style"
    }
  },
  // @swagger
  apis: ["./server.js"]
};

const swaggerSpec = swaggerJsdoc(swaggerOptions);

// route Swagger UI
app.use("/docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.use(express.json());

/**
 * @swagger
 * /search-recipe:
 *   post:
 *     summary: Search recipes by ingredients and style
 *     tags:
 *       - recipes
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - ingredients
 *               - country
 *               - style
 *             properties:
 *               ingredients:
 *                 type: array
 *                 items:
 *                   type: string
 *                 example: ["chicken", "garlic", "lemon"]
 *               country:
 *                 type: string
 *                 example: "Italian"
 *               style:
 *                 type: string
 *                 example: "healthy"
 *     responses:
 *       200:
 *         description: List of matching recipes
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 count:
 *                   type: integer
 *                 recipes:
 *                   type: array
 *                   items:
 *                     type: object
 *                     properties:
 *                       name:
 *                         type: string
 *                       url:
 *                         type: string
 *                       score:
 *                         type: number
 *                       ingredients:
 *                         type: array
 *                         items:
 *                           type: string
 *                       steps:
 *                         type: array
 *                         items:
 *                           type: string
 *       400:
 *         description: Missing or invalid fields
 *       500:
 *         description: Server error
 */

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
