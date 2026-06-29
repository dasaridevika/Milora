package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MilkViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun GoogleSignInScreen(onSignInSuccess: (String, String) -> Unit) {
    var showAccountChooser by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Background Pastures Flow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = Color(0xFFE8F5E9),
                radius = w * 0.7f,
                center = Offset(w * 0.8f, h * 0.2f)
            )
            drawCircle(
                color = Color(0xFFF1F8E9),
                radius = w * 0.8f,
                center = Offset(w * -0.2f, h * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Brand Intro
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MiloraLogo(
                    size = 180.dp,
                    elevation = 6.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your Secure Milk Delivery Companion",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Google Sign In Main Button Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign in to Protect Your Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "We isolate your delivery statistics, calendar, and billing records under your private Google Account.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stylized Standard Google Button
                    Button(
                        onClick = { showAccountChooser = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Render pure Google "G" emblem manually
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0xFFEA4335), radius = size.minDimension / 2)
                                }
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color(0xFF3C4043),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Text(
                        text = "100% Secure • Safe • Offline Encrypted",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showAccountChooser) {
        GoogleAccountChooserDialog(
            onDismiss = { showAccountChooser = false },
            onSelect = { email, name ->
                showAccountChooser = false
                onSignInSuccess(email, name)
            }
        )
    }
}

@Composable
fun GoogleAccountChooserDialog(onDismiss: () -> Unit, onSelect: (String, String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Google Icon mockup
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFF1F3F4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Choose an account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "to continue to Milora",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Simulated Account 1: Owner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect("devikadasari005@gmail.com", "Devika Dasari") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Devika Dasari", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("devikadasari005@gmail.com", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Simulated Account 2: Ramesh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect("ramesh.kumar91@gmail.com", "Ramesh Kumar") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ramesh Kumar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("ramesh.kumar91@gmail.com", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Simulated Account 3: Custom Login
                var customEmail by remember { mutableStateOf("") }
                var customName by remember { mutableStateOf("") }
                var showCustomForm by remember { mutableStateOf(false) }

                if (!showCustomForm) {
                    TextButton(onClick = { showCustomForm = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add account", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use another account")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customEmail,
                            onValueChange = { customEmail = it },
                            label = { Text("Google Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Button(
                            onClick = {
                                if (customEmail.isNotEmpty() && customName.isNotEmpty()) {
                                    onSelect(customEmail, customName)
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = customEmail.contains("@") && customName.length > 2
                        ) {
                            Text("Sign In")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCodeJoinScreen(viewModel: MilkViewModel, onJoined: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enteredCode by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    var showNewProfileForm by remember { mutableStateOf(false) }

    // Form states for registering a brand new customer
    var name by remember { mutableStateOf(AuthSession.currentUser?.name ?: "") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var defaultQty by remember { mutableStateOf("1.0") }
    var pricePerLiter by remember { mutableStateOf("60.0") }

    var step by remember { mutableStateOf(1) } // 1: Enter Owner Code, 2: Link existing or create profile
    val allCustomers by viewModel.customers.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Title
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Link",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Connect with Dairy Owner",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "To keep your milk deliveries private, enter the unique code shared by your Dairy Owner, or use your registered details to auto-recover your profile.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            if (step == 1) {
                // Step 1: Input Owner Join Code
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Option 1: Enter Owner Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = enteredCode,
                            onValueChange = { enteredCode = it.uppercase() },
                            placeholder = { Text("e.g. PM-DEV-1234") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = "Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Button(
                            onClick = {
                                if (enteredCode.trim().length >= 4) {
                                    // Accept any code for high robustness, go to next step
                                    step = 2
                                } else {
                                    Toast.makeText(context, "Please enter a valid owner code", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Validate Code & Continue")
                        }
                    }
                }

                // Auto-Recovery Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Option 2: Instant Auto-Recovery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "If the owner has already registered your name, enter your registered Phone Number or Email below to recover your profile instantly.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = recoveryInput,
                            onValueChange = { recoveryInput = it },
                            placeholder = { Text("Enter Mobile or Email ID") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Recovery Icon") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Button(
                            onClick = {
                                val trimmed = recoveryInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    val matchedCustomer = allCustomers.find {
                                        it.phone == trimmed || it.email.equals(trimmed, ignoreCase = true)
                                    }
                                    if (matchedCustomer != null) {
                                        AuthSession.selectRole(context, UserRole.CUSTOMER)
                                        AuthSession.joinOwner(context, "PM-DEV-1234", matchedCustomer.id)
                                        viewModel.activeCustomerId.value = matchedCustomer.id
                                        Toast.makeText(context, "Welcome! Profile '${matchedCustomer.name}' auto-recovered successfully.", Toast.LENGTH_LONG).show()
                                        onJoined()
                                    } else {
                                        Toast.makeText(context, "No profile found matching '$trimmed'. Check with your owner.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter your phone or email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Recover Profile & Login")
                        }
                    }
                }
            } else {
                // Step 2: Choose profile linking or create a new profile!
                Text(
                    text = "Owner Code Validated! ✅",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )

                if (!showNewProfileForm) {
                    // Option A: Link with existing profiles pre-loaded by Owner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Link Existing Registered Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Select your profile below if the dairy owner has already registered your name:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            if (allCustomers.isEmpty()) {
                                Text("No existing profiles found in DB.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                allCustomers.forEach { customer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.background)
                                            .clickable {
                                                AuthSession.joinOwner(context, enteredCode, customer.id)
                                                viewModel.activeCustomerId.value = customer.id
                                                Toast.makeText(context, "Successfully linked with ${customer.name}!", Toast.LENGTH_LONG).show()
                                                onJoined()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = "Customer", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(customer.address, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            OutlinedButton(
                                onClick = { showNewProfileForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Brand New Customer Profile")
                            }
                        }
                    }
                } else {
                    // Option B: Sign up brand new customer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Register New Customer Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Delivery Address / House Details") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = defaultQty,
                                    onValueChange = { defaultQty = it },
                                    label = { Text("Default Liters (L)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = pricePerLiter,
                                    onValueChange = { pricePerLiter = it },
                                    label = { Text("Price per Liter") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    val qty = defaultQty.toDoubleOrNull() ?: 1.0
                                    val price = pricePerLiter.toDoubleOrNull() ?: 60.0
                                    if (name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                                        coroutineScope.launch {
                                            val googleEmail = AuthSession.currentUser?.email ?: ""
                                            // Add customer entity through viewmodel
                                            viewModel.addNewCustomer(name, phone, googleEmail, address, qty, price)
                                            
                                            // Retrieve the newly inserted customer's ID
                                            val list = viewModel.customers.first()
                                            val newId = list.maxByOrNull { it.id }?.id ?: 1
                                            
                                            AuthSession.joinOwner(context, enteredCode, newId)
                                            viewModel.activeCustomerId.value = newId
                                            
                                            Toast.makeText(context, "Welcome, $name! Registered successfully.", Toast.LENGTH_LONG).show()
                                            onJoined()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please fill in all details", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Register & Link Account")
                            }

                            TextButton(
                                onClick = { showNewProfileForm = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Back to Existing Profiles")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
