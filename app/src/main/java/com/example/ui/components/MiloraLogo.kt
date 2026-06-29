package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MiloraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    elevation: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFFFAF9F5)) // Cream white background
            .border(size * 0.04f, Color(0xFF1B5E20), CircleShape), // Rich green circular border
        contentAlignment = Alignment.Center
    ) {
        // High fidelity vector illustration of the logo elements using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            
            // 1. Draw Rising Sun in background (Upper-Right)
            drawCircle(
                color = Color(0xFFFFF176),
                radius = w * 0.16f,
                center = Offset(w * 0.68f, h * 0.28f)
            )
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                radius = w * 0.20f,
                center = Offset(w * 0.68f, h * 0.28f)
            )

            // 2. Distant Mountains/Hills
            val mountainPath = Path().apply {
                moveTo(0f, h * 0.55f)
                quadraticTo(w * 0.25f, h * 0.38f, w * 0.5f, h * 0.48f)
                quadraticTo(w * 0.75f, h * 0.35f, w, h * 0.50f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(mountainPath, color = Color(0xFFC8E6C9)) // Soft green hills

            val intermediateHill = Path().apply {
                moveTo(0f, h * 0.60f)
                quadraticTo(w * 0.35f, h * 0.48f, w * 0.7f, h * 0.55f)
                quadraticTo(w * 0.85f, h * 0.50f, w, h * 0.58f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(intermediateHill, color = Color(0xFFA5D6A7)) // Darker rolling hills

            // 3. Tree on the Left Side
            // Trunk
            val trunkPath = Path().apply {
                moveTo(w * 0.18f, h * 0.60f)
                quadraticTo(w * 0.19f, h * 0.45f, w * 0.15f, h * 0.30f)
                lineTo(w * 0.22f, h * 0.30f)
                quadraticTo(w * 0.23f, h * 0.48f, w * 0.24f, h * 0.60f)
                close()
            }
            drawPath(trunkPath, color = Color(0xFF5D4037)) // Brown trunk

            // Foliage/Canopy
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = w * 0.15f,
                center = Offset(w * 0.18f, h * 0.25f)
            )
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = w * 0.11f,
                center = Offset(w * 0.24f, h * 0.20f)
            )
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = w * 0.12f,
                center = Offset(w * 0.12f, h * 0.22f)
            )

            // 4. Rustic Cottage (Left Side under the Tree)
            val cottagePath = Path().apply {
                moveTo(w * 0.10f, h * 0.58f)
                lineTo(w * 0.28f, h * 0.58f)
                lineTo(w * 0.28f, h * 0.50f)
                lineTo(w * 0.10f, h * 0.50f)
                close()
            }
            drawPath(cottagePath, color = Color(0xFFE0F2F1)) // White-washed walls
            
            // Cottage roof (Thatched style)
            val roofPath = Path().apply {
                moveTo(w * 0.07f, h * 0.51f)
                lineTo(w * 0.19f, h * 0.41f)
                lineTo(w * 0.31f, h * 0.51f)
                close()
            }
            drawPath(roofPath, color = Color(0xFF8D6E63)) // Brown thatched roof

            // Cottage Door
            drawRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(w * 0.17f, h * 0.53f),
                size = Size(w * 0.05f, h * 0.05f)
            )

            // 5. Water Buffalo (Center Face Representation)
            // Buffalo face container/head
            val buffaloHeadPath = Path().apply {
                moveTo(w * 0.38f, h * 0.42f)
                quadraticTo(w * 0.50f, h * 0.38f, w * 0.62f, h * 0.42f)
                lineTo(w * 0.58f, h * 0.58f)
                quadraticTo(w * 0.50f, h * 0.64f, w * 0.42f, h * 0.58f)
                close()
            }
            drawPath(buffaloHeadPath, color = Color(0xFF212121)) // Black water buffalo head

            // Curved Horn Left
            val leftHornPath = Path().apply {
                moveTo(w * 0.40f, h * 0.41f)
                cubicTo(w * 0.32f, h * 0.40f, w * 0.30f, h * 0.32f, w * 0.32f, h * 0.26f)
                cubicTo(w * 0.36f, h * 0.25f, w * 0.39f, h * 0.35f, w * 0.42f, h * 0.39f)
                close()
            }
            drawPath(leftHornPath, color = Color(0xFF424242)) // Gray horn

            // Curved Horn Right
            val rightHornPath = Path().apply {
                moveTo(w * 0.60f, h * 0.41f)
                cubicTo(w * 0.68f, h * 0.40f, w * 0.70f, h * 0.32f, w * 0.68f, h * 0.26f)
                cubicTo(w * 0.64f, h * 0.25f, w * 0.61f, h * 0.35f, w * 0.58f, h * 0.39f)
                close()
            }
            drawPath(rightHornPath, color = Color(0xFF424242)) // Gray horn

            // Buffalo Muzzle and Snout details
            drawCircle(
                color = Color(0xFF111111),
                radius = w * 0.08f,
                center = Offset(w * 0.50f, h * 0.56f)
            )
            // Nostrils
            drawCircle(color = Color.Black, radius = w * 0.012f, center = Offset(w * 0.47f, h * 0.56f))
            drawCircle(color = Color.Black, radius = w * 0.012f, center = Offset(w * 0.53f, h * 0.56f))

            // Eyes
            drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(w * 0.44f, h * 0.46f))
            drawCircle(color = Color.Black, radius = w * 0.006f, center = Offset(w * 0.44f, h * 0.46f))
            drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(w * 0.56f, h * 0.46f))
            drawCircle(color = Color.Black, radius = w * 0.006f, center = Offset(w * 0.56f, h * 0.46f))

            // Buffalo Collar and Brass Bell
            drawLine(
                color = Color(0xFF8D6E63),
                start = Offset(w * 0.41f, h * 0.58f),
                end = Offset(w * 0.59f, h * 0.58f),
                strokeWidth = w * 0.02f
            )
            // Golden Bell
            val bellPath = Path().apply {
                moveTo(w * 0.47f, h * 0.59f)
                lineTo(w * 0.53f, h * 0.59f)
                lineTo(w * 0.54f, h * 0.64f)
                lineTo(w * 0.46f, h * 0.64f)
                close()
            }
            drawPath(bellPath, color = Color(0xFFFFC107)) // Golden brass bell
            drawCircle(color = Color(0xFFFFD54F), radius = w * 0.012f, center = Offset(w * 0.50f, h * 0.645f))

            // 6. Polished Steel Milk Can & Splash (Middle Right)
            // Can Body
            val canPath = Path().apply {
                moveTo(w * 0.66f, h * 0.48f)
                lineTo(w * 0.82f, h * 0.48f)
                lineTo(w * 0.85f, h * 0.55f)
                lineTo(w * 0.85f, h * 0.70f)
                lineTo(w * 0.63f, h * 0.70f)
                lineTo(w * 0.63f, h * 0.55f)
                close()
            }
            // Draw can with metallic gradient
            drawPath(
                path = canPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE)),
                    start = Offset(w * 0.63f, h * 0.55f),
                    end = Offset(w * 0.85f, h * 0.55f)
                )
            )

            // Can lid
            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(w * 0.70f, h * 0.44f),
                size = Size(w * 0.08f, h * 0.04f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )
            // Can handle
            drawArc(
                color = Color(0xFF546E7A),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.62f, h * 0.51f),
                size = Size(w * 0.06f, h * 0.08f),
                style = Stroke(width = w * 0.012f)
            )

            // Pure White Milk Splash
            val splashPath = Path().apply {
                moveTo(w * 0.60f, h * 0.64f)
                quadraticTo(w * 0.58f, h * 0.55f, w * 0.65f, h * 0.58f)
                quadraticTo(w * 0.72f, h * 0.48f, w * 0.76f, h * 0.56f)
                quadraticTo(w * 0.84f, h * 0.52f, w * 0.82f, h * 0.62f)
                quadraticTo(w * 0.88f, h * 0.64f, w * 0.80f, h * 0.68f)
                quadraticTo(w * 0.70f, h * 0.73f, w * 0.60f, h * 0.64f)
                close()
            }
            drawPath(splashPath, color = Color.White)

            // Droplets splashing out
            drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.56f, h * 0.55f))
            drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(w * 0.74f, h * 0.46f))
            drawCircle(color = Color.White, radius = w * 0.010f, center = Offset(w * 0.86f, h * 0.53f))

            // 7. Sweeping White Ribbon/Banner Container
            // A graceful wave across the bottom portion of the circle
            val bannerPath = Path().apply {
                moveTo(0f, h * 0.65f)
                cubicTo(w * 0.25f, h * 0.56f, w * 0.75f, h * 0.72f, w, h * 0.62f)
                lineTo(w, h * 0.88f)
                cubicTo(w * 0.75f, h * 0.94f, w * 0.25f, h * 0.82f, 0f, h * 0.88f)
                close()
            }
            drawPath(bannerPath, color = Color.White) // Bright clean banner
            
            // Thin elegant outline on the banner
            drawPath(
                path = bannerPath,
                color = Color(0xFF1B5E20),
                style = Stroke(width = w * 0.01f)
            )

            // 8. Agricultural rows at the absolute bottom
            val bottomFieldPath = Path().apply {
                moveTo(0f, h * 0.88f)
                cubicTo(w * 0.25f, h * 0.82f, w * 0.75f, h * 0.94f, w, h * 0.88f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(bottomFieldPath, color = Color(0xFF1B5E20)) // Deep green footer

            // Draw field crop rows (light green stripes)
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(w * 0.2f, h * 0.90f),
                end = Offset(w * 0.1f, h),
                strokeWidth = w * 0.02f
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(w * 0.4f, h * 0.92f),
                end = Offset(w * 0.35f, h),
                strokeWidth = w * 0.02f
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(w * 0.6f, h * 0.92f),
                end = Offset(w * 0.65f, h),
                strokeWidth = w * 0.02f
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(w * 0.8f, h * 0.90f),
                end = Offset(w * 0.9f, h),
                strokeWidth = w * 0.02f
            )

            // 9. Small Farmhouse silhouette at the very bottom
            val smallHousePath = Path().apply {
                moveTo(w * 0.44f, h * 0.94f)
                lineTo(w * 0.56f, h * 0.94f)
                lineTo(w * 0.56f, h * 0.90f)
                lineTo(w * 0.44f, h * 0.90f)
                close()
            }
            drawPath(smallHousePath, color = Color.White)
            val smallHouseRoof = Path().apply {
                moveTo(w * 0.42f, h * 0.91f)
                lineTo(w * 0.50f, h * 0.86f)
                lineTo(w * 0.58f, h * 0.91f)
                close()
            }
            drawPath(smallHouseRoof, color = Color(0xFFD84315)) // Terracotta roof
        }

        // Overlay text dynamically using compose layout so we get crisp fonts at any resolution
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = size * 0.05f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(3.4f))

            // Brand Text "Milora" centered inside the white banner area
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Milora",
                    fontSize = (size.value * 0.16f).sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF113B16), // Hunter green
                    textAlign = TextAlign.Center,
                    lineHeight = (size.value * 0.17f).sp
                )

                // Beautiful leaf motif above the letter 'o'
                // Placement: slightly right of center, offset upwards
                Box(
                    modifier = Modifier
                        .offset(x = size * 0.09f, y = -size * 0.06f)
                        .size(size * 0.08f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cw = this.size.width
                        val leafPath = Path().apply {
                            moveTo(0f, cw)
                            cubicTo(cw * 0.2f, cw * 0.2f, cw * 0.8f, 0f, cw, 0f)
                            cubicTo(cw * 0.8f, cw * 0.8f, cw * 0.2f, cw * 0.9f, 0f, cw)
                            close()
                        }
                        drawPath(leafPath, color = Color(0xFF4CAF50))
                    }
                }
            }

            Spacer(modifier = Modifier.height(size * 0.005f))

            // Tagline: "Pure Milk. Pure Trust."
            Text(
                text = "Pure Milk. Pure Trust.",
                fontSize = (size.value * 0.052f).sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = size * 0.01f)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
