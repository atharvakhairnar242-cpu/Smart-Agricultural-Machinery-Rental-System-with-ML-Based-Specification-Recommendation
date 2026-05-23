package com.example.khetseva3
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import okhttp3.ResponseBody
import com.example.khetseva3.model.*
import com.example.khetseva3.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.draw.blur
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.key
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences(
                "USER",
                Context.MODE_PRIVATE
            )

            var isLoggedIn by remember {
                mutableStateOf(
                    prefs.getString("phone", null) != null
                )
            }

            var languageExpanded by remember { mutableStateOf(false) }
            var currentLanguage by remember { mutableStateOf("en") }
            var screen by rememberSaveable {
                mutableStateOf(
                    if (isLoggedIn) "role" else "login"
                )
            }
            var apiResult by remember { mutableStateOf<RecommendResponse?>(null) }
            var isForward by remember { mutableStateOf(true) }
            var previousTakeForm by rememberSaveable {
                mutableStateOf("takeTractorForm")
            }
            var previousScreen by rememberSaveable {
                mutableStateOf("role")
            }

            fun changeLanguage(languageCode: String) {
                currentLanguage = languageCode
                val locale = java.util.Locale(languageCode)
                java.util.Locale.setDefault(locale)
                val config = context.resources.configuration
                config.setLocale(locale)
                context.resources.updateConfiguration(
                    config,
                    context.resources.displayMetrics
                )
            }

            fun navigateForward(route: String) {
                isForward = true
                screen = route
            }

            fun goBack() {
                isForward = false
                screen = when (screen) {

                    // TAKE FLOW
                    "recommendation" -> previousTakeForm
                    "takeTractorForm",
                    "takeHarvesterForm",
                    "takeSeedDrillForm",
                    "takeRotavatorForm" -> "takeSelect"
                    "myMachines" -> "role"
                    "takeSelect" -> "role"

                    // GIVE FLOW
                    "giveTractorForm",
                    "giveHarvesterForm",
                    "giveSeedDrillForm",
                    "giveRotavatorForm" -> "giveSelect"

                    "giveSelect" -> "role"

                    "userProfile" -> previousScreen
                    "register" -> "login"

                    else -> "role"
                }
            }

            if (!isLoggedIn && screen !in listOf("login", "register")) {
                screen = "login"
            }

            MaterialTheme {

                key(currentLanguage) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {

                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                if (isForward) {
                                    slideInHorizontally { it } togetherWith
                                            slideOutHorizontally { -it }
                                } else {
                                    slideInHorizontally { -it } togetherWith
                                            slideOutHorizontally { it }
                                }
                            },
                            label = "screenAnimation"
                        ) { targetScreen ->

                            when (targetScreen) {

                                // ================= LOGIN =================
                                "login" -> {
                                    Box(Modifier.fillMaxSize()) {

                                        LoginScreen(

                                            onLogin = { phone, password ->

                                                val request = LoginRequest(
                                                    phone = phone,
                                                    password = password
                                                )

                                                RetrofitClient.api.login(request)
                                                    .enqueue(object : Callback<LoginResponse> {

                                                        override fun onResponse(
                                                            call: Call<LoginResponse>,
                                                            response: Response<LoginResponse>
                                                        ) {

                                                            if (response.isSuccessful) {

                                                                val user = response.body()

                                                                val prefs = context.getSharedPreferences(
                                                                    "USER",
                                                                    Context.MODE_PRIVATE
                                                                )

                                                                prefs.edit()
                                                                    .putString("name", user?.name)
                                                                    .putString("phone", user?.phone)
                                                                    .putString("email", user?.email)
                                                                    .putString("country", user?.country)
                                                                    .putString("state", user?.state)
                                                                    .putString("city", user?.city)
                                                                    .apply()

                                                                isLoggedIn = true
                                                                Toast.makeText(
                                                                    context,
                                                                    "Login successful",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                navigateForward("role")

                                                            } else {

                                                                Toast.makeText(
                                                                    context,
                                                                    "Invalid phone or password",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                Log.e("LOGIN", "Login failed")
                                                            }
                                                        }

                                                        override fun onFailure(
                                                            call: Call<LoginResponse>,
                                                            t: Throwable
                                                        ) {
                                                            Log.e("LOGIN", "FAILED: ${t.message}")
                                                        }
                                                    })
                                            },

                                            onRegisterClick = {
                                                navigateForward("register")
                                            }
                                        )


                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .statusBarsPadding()
                                                .padding(16.dp)
                                        ) {
                                            LanguageMenu(
                                                expanded = languageExpanded,
                                                onExpand = { languageExpanded = true },
                                                onDismiss = { languageExpanded = false },
                                                onSelect = {
                                                    changeLanguage(it)
                                                    languageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // ================= REGISTER =================
                                "register" -> {
                                    Box(Modifier.fillMaxSize()) {

                                        RegisterScreen(onBack = { goBack() })

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {

                                            TextButton(onClick = { goBack() }) {
                                                Text(
                                                    text = stringResource(R.string.back),
                                                    color = Color.White
                                                )
                                            }

                                            LanguageMenu(
                                                expanded = languageExpanded,
                                                onExpand = { languageExpanded = true },
                                                onDismiss = { languageExpanded = false },
                                                onSelect = {
                                                    changeLanguage(it)
                                                    languageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // ================= AFTER LOGIN =================
                                else -> {

                                    Column(Modifier.fillMaxSize()) {

                                        // HEADER
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Image(
                                                painter = painterResource(id = R.drawable.logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(120.dp)
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                LanguageMenu(
                                                    expanded = languageExpanded,
                                                    onExpand = { languageExpanded = true },
                                                    onDismiss = { languageExpanded = false },
                                                    onSelect = {
                                                        changeLanguage(it)
                                                        languageExpanded = false
                                                    }
                                                )

                                                Spacer(Modifier.width(4.dp))

                                                IconButton(

                                                    onClick = {
                                                        previousScreen = screen
                                                        navigateForward("userProfile") }
                                                ) {
                                                    Text("👤")
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        if (targetScreen != "role") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp)
                                            ) {
                                                PremiumBackButton { goBack() }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                        ) {

                                            when (targetScreen) {

                                                // ROLE
                                                "role" -> RoleScreen(
                                                    onTake = { navigateForward("takeSelect") },
                                                    onGive = { navigateForward("giveSelect") },
                                                    onBack = {},
                                                    onMyMachines = {
                                                        navigateForward("myMachines")
                                                    }

                                                )


                                                // TAKE SELECT
                                                "takeSelect" -> MachinerySelectScreen(
                                                    title = stringResource(R.string.select_machine),
                                                    onTractorClick = { navigateForward("takeTractorForm") },
                                                    onHarvesterClick = { navigateForward("takeHarvesterForm") },
                                                    onSeedDrillClick = { navigateForward("takeSeedDrillForm") },
                                                    onRotavatorClick = { navigateForward("takeRotavatorForm") }
                                                )
                                                "recommendation" -> RecommendationScreen(
                                                    result = apiResult,
                                                    onBack = { goBack() }
                                                )
                                                // TAKE FORMS
                                                "takeTractorForm" ->
                                                    TakeTractorForm { farmSize, usage, soil, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "tractor",                // ✅ FIX 1 (lowercase)
                                                            pricing_type = pricing,          // ✅ FIX 2 (NO mapping)
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                operation = usage,           // ✅ correct
                                                                soil_type = soil,            // ✅ correct
                                                                budget = budget
                                                            )
                                                        )

                                                        // optional but good (same as harvester)
                                                        apiResult = null

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeTractorForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }

                                                "takeHarvesterForm" ->
                                                    TakeHarvesterForm { farmSize, crop, budget, pricing ->

                                                        Log.e("API_TEST", "Button clicked")

                                                        val request = RecommendRequest(
                                                            type = "Harvester",
                                                            pricing_type = pricing,
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                crop_type = crop,
                                                                budget = budget
                                                            )
                                                        )

                                                        previousTakeForm = "takeHarvesterForm"
                                                        screen = "recommendation"

                                                        // 🔥 Reset old data
                                                        apiResult = null

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {

                                                                    Log.e("API_TEST", "Response received")

                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()   // 🔥 THIS WAS MISSING
                                                                    } else {
                                                                        Log.e("API_TEST", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API_TEST", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }

                                                "takeSeedDrillForm" ->
                                                    TakeSeedDrillForm { farmSize, crop, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "seed drill",        // ✅ FIX 1 (match backend)
                                                            pricing_type = pricing,     // ✅ FIX 2 (NO mapping)
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                crop_type = crop,
                                                                budget = budget
                                                            )
                                                        )

                                                        apiResult = null   // optional (loading state)

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeSeedDrillForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }

                                                "takeRotavatorForm" ->
                                                    TakeRotavatorForm { hp, soil, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "rotavator",          // ✅ FIX 1 (lowercase)
                                                            pricing_type = pricing,      // ✅ FIX 2 (NO mapping)
                                                            inputs = Inputs(
                                                                tractor_hp = hp.toFloat(),
                                                                soil_type = soil,
                                                                budget = budget
                                                            )
                                                        )

                                                        apiResult = null   // optional but good (loading state)

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeRotavatorForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }

                                                // GIVE SELECT
                                                "giveSelect" -> MachinerySelectScreen(
                                                    title = stringResource(R.string.select_machine),
                                                    onTractorClick = { navigateForward("giveTractorForm") },
                                                    onHarvesterClick = { navigateForward("giveHarvesterForm") },
                                                    onSeedDrillClick = { navigateForward("giveSeedDrillForm") },
                                                    onRotavatorClick = { navigateForward("giveRotavatorForm") }
                                                )

                                                // GIVE FORMS
                                                "giveTractorForm" -> GiveTractorForm()
                                                "giveHarvesterForm" -> GiveHarvesterForm()
                                                "giveSeedDrillForm" -> GiveSeedDrillForm()
                                                "giveRotavatorForm" -> GiveRotavatorForm()
                                                "myMachines" -> MyMachinesScreen()
                                                // PROFILE
                                                // PROFILE
                                                "userProfile" -> {

                                                    val prefs = context.getSharedPreferences(
                                                        "USER",
                                                        Context.MODE_PRIVATE
                                                    )

                                                    UserProfileScreen(
                                                        name = prefs.getString("name", "") ?: "",
                                                        phone = prefs.getString("phone", "") ?: "",

                                                        onLogout = {

                                                            prefs.edit().clear().apply()

                                                            isLoggedIn = false
                                                            navigateForward("login")
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageMenu(
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Box {
        IconButton(onClick = onExpand) {
            Text("🌐")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            DropdownMenuItem(
                text = { Text("English") },
                onClick = { onSelect("en") }
            )
            DropdownMenuItem(
                text = { Text("हिन्दी") },
                onClick = { onSelect("hi") }
            )
            DropdownMenuItem(
                text = { Text("मराठी") },
                onClick = { onSelect("mr") }
            )
        }
    }
}




@Composable
fun UserProfileScreen(
    name: String,
    phone: String,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "👤 " + stringResource(R.string.user_profile),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "👤 $name",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📞 $phone",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),   // Red background
                contentColor = Color.White           // White text
            )
        ) {
            Text(stringResource(R.string.logout))
        }

    }
}


/* ---------------- PREMIUM BACK BUTTON ---------------- */

@Composable
fun PremiumBackButton(onBack: () -> Unit) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F5E9))
            .clickable { onBack() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.back),   // ✅ FIXED
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E7D32)
        )
    }
}



/* ---------------- LOGIN ---------------- */

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val phoneErrorText = stringResource(R.string.phone_error)
    val passwordErrorText = stringResource(R.string.password_error) // ✅ NEW

    val loginText = stringResource(R.string.login)
    val createAccountText = stringResource(R.string.create_account)

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.loginpic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.65f))
                .padding(24.dp)
        ) {

            Column {

                Text(
                    text = stringResource(R.string.welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 10) {
                            phone = it
                        }
                    },
                    label = { Text(stringResource(R.string.phone)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔐 PASSWORD (Multi-language)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            phone.length != 10 -> error = phoneErrorText
                            password.length < 6 -> error = passwordErrorText
                            else -> {
                                error = ""
                                onLogin(phone, password)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(loginText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = createAccountText,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
fun RegisterScreen(onBack: () -> Unit) {

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    val registerText = stringResource(R.string.register)
    val errorText = stringResource(R.string.fill_details_error)
    val successText = stringResource(R.string.registration_success)
    val passwordErrorText = stringResource(R.string.password_error)
    val locationText = stringResource(R.string.give_location_access)

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔹 Background
        Image(
            painter = painterResource(id = R.drawable.loginpic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        // 🔥 MAIN LAYOUT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 🔹 TOP BAR (Back + Language)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Back Button
                TextButton(onClick = onBack) {
                    Text("←")
                }

                // Language Icon (placeholder)

            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 PANEL CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),   // IMPORTANT
                contentAlignment = Alignment.TopCenter
            ) {

                // 🔥 SCROLLABLE GLASS PANEL
                Column(
                    modifier = Modifier
                        .width(320.dp)   // 👈 manual control size
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(14.5.dp))
                        .background(Color.White.copy(alpha = 0.65f))
                        .padding(20.dp)
                ) {

                    Text(
                        text = registerText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() } && it.length <= 10)
                                phone = it
                        },
                        label = { Text(stringResource(R.string.phone)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.set_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Country
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text(stringResource(R.string.country)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // State
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text(stringResource(R.string.state)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // City
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(stringResource(R.string.city)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Register Button
                    Button(
                        onClick = {
                            Log.e("REGISTER", "BUTTON CLICKED")
                            if (
                                name.isBlank() ||
                                email.isBlank() ||
                                phone.length != 10 ||
                                password.length < 6
                            ) {
                                error = errorText
                            } else {
                                error = ""
                                val request = RegisterRequest(
                                    name = name,
                                    phone = phone,
                                    password = password,
                                    country = country,
                                    state = state,
                                    city = city,
                                    email = email
                                )

                                RetrofitClient.api.register(request)
                                    .enqueue(object : Callback<Map<String, String>> {

                                        override fun onResponse(

                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {
                                            Log.e("REGISTER", "CODE: ${response.code()}")
                                            if (response.isSuccessful) {

                                                Toast.makeText(
                                                    context,
                                                    successText,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                onBack()

                                            } else {
                                                error = "Registration failed"
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {
                                            Log.e("REGISTER", "FAILED: ${t.message}")

                                            error = t.message ?: "Network error"
                                        }
                                    })
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(registerText)
                    }

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = Color.Red)
                    }
                }
            }
        }
    }
}
/* ---------------- ROLE ---------------- */

@Composable
fun RoleScreen(
    onTake: () -> Unit,
    onGive: () -> Unit,
    onBack: () -> Unit,
    onMyMachines: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // 🔹 Centered Title
        Text(
            text = stringResource(R.string.select_machine),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 🚜 TAKE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTake() },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                Text(
                    text = "🚜",
                    fontSize = 34.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.take_rent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.take_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 🏭 GIVE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onGive() },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F8E9)
            )

        ) {

            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                Text(
                    text = "🏭",
                    fontSize = 34.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.give_rent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.give_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            Button(
                onClick = {
                    onMyMachines()
                }
            ) {
                Text("🚜 My Machines")
            }
        }
    }
}




/* ---------------- MACHINERY SELECT ---------------- */

@Composable
fun MachinerySelectScreen(
    title: String,
    onTractorClick: () -> Unit,
    onHarvesterClick: () -> Unit,
    onSeedDrillClick: () -> Unit,
    onRotavatorClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🚜 TRACTOR
        MachineCard(
            emoji = "🚜",
            text = stringResource(R.string.tractor),
            onClick = onTractorClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🌾 HARVESTER
        MachineCard(
            emoji = "🌾",
            text = stringResource(R.string.harvester),
            onClick = onHarvesterClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🌱 SEED DRILL
        MachineCard(
            emoji = "🌱",
            text = stringResource(R.string.seed_drill),
            onClick = onSeedDrillClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ⚙️ ROTAVATOR
        MachineCard(
            emoji = "⚙️",
            text = stringResource(R.string.rotavator),
            onClick = onRotavatorClick
        )
    }
}

data class Option(
    val label: String,   // UI (Hindi/Marathi/English)
    val value: String    // ML value (always English)
)

/* ---------------- TAKE FORM  - tractor---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeTractorForm(
    onNext: (String, String, String, String, String) -> Unit
) {

    var farmSize by remember { mutableStateOf("") }

    // 🔥 STORED IN ENGLISH ONLY
    var usage by remember { mutableStateOf("") }
    var soil by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }

    // 🔽 DROPDOWN STATES
    var usageExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf("") }

    val fillError = stringResource(R.string.fill_all_fields)

    // 🔥 OPTIONS (LABEL = LOCALIZED UI, VALUE = ENGLISH BACKEND)

    val usageOptions = listOf(
        Option(stringResource(R.string.ploughing), "Ploughing"),
        Option(stringResource(R.string.spraying), "Spraying"),
        Option(stringResource(R.string.seeding), "Seeding"),
        Option(stringResource(R.string.transport), "Transport")
    )

    val soilOptions = listOf(
        Option(stringResource(R.string.clay_soil), "Clay"),
        Option(stringResource(R.string.sandy_soil), "Sandy"),
        Option(stringResource(R.string.loamy_soil), "Loamy")
    )

    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )

    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = stringResource(R.string.tractor_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 🔹 Farm Size (FREE INPUT — LEFT AS IS)
        OutlinedTextField(
            value = farmSize,
            onValueChange = { farmSize = it },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Usage
        DropdownField(
            value = usageOptions.find { it.value == usage }?.label ?: "",
            label = stringResource(R.string.select_usage),
            options = usageOptions.map { it.label },
            expanded = usageExpanded,
            onExpandChange = { usageExpanded = it },
            onSelected = { selectedLabel ->
                usage = usageOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Soil
        DropdownField(
            value = soilOptions.find { it.value == soil }?.label ?: "",
            label = stringResource(R.string.select_soil),
            options = soilOptions.map { it.label },
            expanded = soilExpanded,
            onExpandChange = { soilExpanded = it },
            onSelected = { selectedLabel ->
                soil = soilOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Budget
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔥 Pricing Preference (NEW)
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (
                    farmSize.isBlank() ||
                    usage.isBlank() ||
                    soil.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""

                    // 🔥 FINAL DATA (ALL ENGLISH FOR BACKEND)
                    /*
                        farmSize -> "5" (user input)
                        usage   -> Ploughing / Spraying / Seeding / Transport
                        soil    -> Clay / Sandy / Loamy
                        budget  -> Low / Medium / High
                        pricing -> PerHour / PerDay / PerWeek / PerMonth
                    */

                    onNext(farmSize, usage, soil, budget, pricing)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeHarvesterForm(
    onNext: (String, String, String, String) -> Unit
) {

    var farmSize by remember { mutableStateOf("") }

    // 🔥 STORED IN ENGLISH ONLY
    var crop by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") } // 🔥 NEW

    // 🔽 DROPDOWN STATES
    var cropExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) } // 🔥 NEW

    var error by remember { mutableStateOf("") }

    val fillError = stringResource(R.string.fill_all_fields)

    // 🔥 OPTIONS (LABEL = LOCALIZED, VALUE = ENGLISH)

    val cropOptions = listOf(
        Option(stringResource(R.string.wheat), "Wheat"),
        Option(stringResource(R.string.rice), "Rice"),
        Option(stringResource(R.string.maize), "Maize")
    )

    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )

    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.harvester_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 🔹 Farm Size (FREE INPUT)
        OutlinedTextField(
            value = farmSize,
            onValueChange = { farmSize = it },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Crop (mapped)
        DropdownField(
            value = cropOptions.find { it.value == crop }?.label ?: "",
            label = stringResource(R.string.select_crop),
            options = cropOptions.map { it.label },
            expanded = cropExpanded,
            onExpandChange = { cropExpanded = it },
            onSelected = { selectedLabel ->
                crop = cropOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Budget (mapped)
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔥 NEW: Pricing Preference
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onNext(farmSize, crop, budget, pricing)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeSeedDrillForm(
    onNext: (String, String, String, String) -> Unit
) {

    var farmSize by remember { mutableStateOf("") }

    // 🔥 STORED IN ENGLISH ONLY
    var crop by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") } // 🔥 NEW

    // 🔽 DROPDOWN STATES
    var cropExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) } // 🔥 NEW

    var error by remember { mutableStateOf("") }

    val fillError = stringResource(R.string.fill_all_fields)

    // 🔥 OPTIONS (LABEL = LOCALIZED, VALUE = ENGLISH)

    val cropOptions = listOf(
        Option(stringResource(R.string.wheat), "Wheat"),
        Option(stringResource(R.string.rice), "Rice"),
        Option(stringResource(R.string.maize), "Maize"),
        Option(stringResource(R.string.soybean), "Soybean"),
        Option(stringResource(R.string.cotton), "Cotton")
    )

    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )

    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.seed_drill_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 🔹 Farm Size (FREE INPUT)
        OutlinedTextField(
            value = farmSize,
            onValueChange = { farmSize = it },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Crop (mapped)
        DropdownField(
            value = cropOptions.find { it.value == crop }?.label ?: "",
            label = stringResource(R.string.select_crop),
            options = cropOptions.map { it.label },
            expanded = cropExpanded,
            onExpandChange = { cropExpanded = it },
            onSelected = { selectedLabel ->
                crop = cropOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Budget (mapped)
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔥 NEW: Pricing Preference
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (
                    farmSize.isBlank() ||
                    crop.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""

                    // 🔥 FINAL BACKEND VALUES (ALL ENGLISH)
                    /*
                        farmSize -> "5"
                        crop     -> Wheat / Rice / Maize / Soybean / Cotton
                        budget   -> Low / Medium / High
                        pricing  -> PerHour / PerDay / PerWeek / PerMonth
                    */

                    onNext(farmSize, crop, budget, pricing)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeRotavatorForm(
    onNext: (String, String, String, String) -> Unit
) {

    var hp by remember { mutableStateOf("") }

    // 🔥 STORED IN ENGLISH ONLY
    var soil by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") } // 🔥 NEW

    // 🔽 DROPDOWN STATES
    var soilExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) } // 🔥 NEW

    var error by remember { mutableStateOf("") }

    val fillError = stringResource(R.string.fill_all_fields)

    // 🔥 OPTIONS (LABEL = LOCALIZED, VALUE = ENGLISH)

    val soilOptions = listOf(
        Option(stringResource(R.string.clay_soil), "Clay"),
        Option(stringResource(R.string.loamy_soil), "Loamy"),
        Option(stringResource(R.string.rocky_soil), "Rocky")
    )

    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )

    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.rotavator_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 🔹 Tractor HP (FREE NUMERIC INPUT)
        OutlinedTextField(
            value = hp,
            onValueChange = { hp = it },
            label = { Text(stringResource(R.string.tractor_hp)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Soil (mapped)
        DropdownField(
            value = soilOptions.find { it.value == soil }?.label ?: "",
            label = stringResource(R.string.select_soil),
            options = soilOptions.map { it.label },
            expanded = soilExpanded,
            onExpandChange = { soilExpanded = it },
            onSelected = { selectedLabel ->
                soil = soilOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 Budget (mapped)
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔥 NEW: Pricing Preference
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (
                    hp.isBlank() ||
                    soil.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""

                    // 🔥 FINAL BACKEND VALUES (ALL ENGLISH)
                    /*
                        hp      -> "45"
                        soil    -> Clay / Loamy / Rocky
                        budget  -> Low / Medium / High
                        pricing -> PerHour / PerDay / PerWeek / PerMonth
                    */

                    onNext(hp, soil, budget, pricing)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}

/* ---------------- GIVE FORM ---------------- */

@Composable
fun GiveTractorForm() {
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var uploadedImageUrl by remember {
        mutableStateOf("")
    }
    var hpRange by remember { mutableStateOf("") }
    var tractorModel by remember { mutableStateOf("") }
    val context = LocalContext.current
    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val ownerEmail = prefs.getString("email", "") ?: ""
    var error by remember { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        selectedImageUri = uri
    }
    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {

        Text(stringResource(R.string.tractor_requirements),
            style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(24.dp))

        // 🔥 ONLY NUMBERS
        OutlinedTextField(
            value = hpRange,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) hpRange = it
            },
            label = { Text(stringResource(R.string.tractor_hp_range)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = tractorModel,
            onValueChange = { tractorModel = it },
            label = { Text(stringResource(R.string.tractor_model_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.pricing_details))

        Spacer(Modifier.height(12.dp))

        PriceFields(perHour, { perHour = it }, perDay, { perDay = it },
            perWeek, { perWeek = it }, perMonth, { perMonth = it })
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 Upload Machine Image")
        }
        selectedImageUri?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(onClick = {

            if (hpRange.isBlank() || tractorModel.isBlank()) {
                error = fillErrorText
                return@Button
            }

            error = ""
            val prefs = context.getSharedPreferences(
                "USER",
                Context.MODE_PRIVATE
            )

            val ownerName = prefs.getString("name", "") ?: ""
            val ownerPhone = prefs.getString("phone", "") ?: ""
            val city = prefs.getString("city", "") ?: ""
            if (selectedImageUri != null) {

                val file = uriToFile(context, selectedImageUri!!)

                val requestFile = file.asRequestBody(
                    "image/*".toMediaTypeOrNull()
                )

                val body = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestFile
                )

                RetrofitClient.api.uploadImage(body)
                    .enqueue(object : Callback<ImageUploadResponse> {

                        override fun onResponse(
                            call: Call<ImageUploadResponse>,
                            response: Response<ImageUploadResponse>
                        ) {

                            if (response.isSuccessful) {

                                uploadedImageUrl =
                                    response.body()?.image_url ?: ""
                                val request = AddMachineRequest(
                                    type = "tractor",
                                    model_name = tractorModel,
                                    hp_range = hpRange.toInt(),

                                    cutting_width = null,
                                    working_width = null,
                                    row_count = null,

                                    price_per_hour = perHour.toIntOrNull(),
                                    price_per_day = perDay.toIntOrNull(),
                                    price_per_week = perWeek.toIntOrNull(),
                                    price_per_month = perMonth.toIntOrNull(),

                                    owner_name = ownerName,
                                    owner_phone = ownerPhone,
                                    location = city,
                                    owner_email = ownerEmail,

                                    image_url = uploadedImageUrl
                                )

                                RetrofitClient.api.addMachine(request)
                                    .enqueue(object : Callback<Map<String, String>> {

                                        override fun onResponse(
                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {

                                            if (response.isSuccessful) {

                                                Log.e("API", "✅ Machine Added")

                                                Toast.makeText(
                                                    context,
                                                    "🚜 Machine registered successfully!",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                hpRange = ""
                                                tractorModel = ""
                                                perHour = ""
                                                perDay = ""
                                                perWeek = ""
                                                perMonth = ""

                                                selectedImageUri = null

                                            } else {

                                                Log.e("API", "❌ Error: ${response.code()}")
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {

                                            Log.e("API", "❌ FAILED: ${t.message}")
                                        }
                                    })

                            }
                        }

                        override fun onFailure(
                            call: Call<ImageUploadResponse>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                context,
                                "Image upload failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }


        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) Text(error, color = Color.Red)
    }
}
fun uriToFile(context: Context, uri: Uri): File {

    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("upload", ".jpg", context.cacheDir)

    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}
@Composable
fun GiveHarvesterForm() {

    var cuttingWidth by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }

    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )

    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""

    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.harvester_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 🔥 ONLY NUMBERS
        OutlinedTextField(
            value = cuttingWidth,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    cuttingWidth = it
                }
            },
            label = {
                Text(stringResource(R.string.cutting_width_ft))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.harvester_model_name))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.pricing_details))

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    cuttingWidth.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }

                error = ""

                val request = AddMachineRequest(

                    type = "harvester",

                    model_name = modelName,

                    hp_range = null,

                    cutting_width = cuttingWidth.toDouble(),

                    working_width = null,

                    row_count = null,

                    price_per_hour = perHour.toIntOrNull(),
                    price_per_day = perDay.toIntOrNull(),
                    price_per_week = perWeek.toIntOrNull(),
                    price_per_month = perMonth.toIntOrNull(),

                    owner_name = ownerName,
                    owner_phone = ownerPhone,
                    owner_email = ownerEmail,
                    location = city
                )

                RetrofitClient.api.addMachine(request)

                    .enqueue(object : Callback<Map<String, String>> {

                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {

                            if (response.isSuccessful) {

                                Toast.makeText(
                                    context,
                                    "🚜 Harvester registered successfully!",
                                    Toast.LENGTH_LONG
                                ).show()

                                cuttingWidth = ""
                                modelName = ""

                                perHour = ""
                                perDay = ""
                                perWeek = ""
                                perMonth = ""
                            }

                            else {

                                Toast.makeText(
                                    context,
                                    "Failed to register harvester",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, String>>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                context,
                                "Failed: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}

@Composable
fun GiveSeedDrillForm() {

    var rowCount by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }

    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )

    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""

    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.seed_drill_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = rowCount,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    rowCount = it
                }
            },
            label = {
                Text(stringResource(R.string.row_count))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.seed_drill_model_name))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.pricing_details))

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    rowCount.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }

                error = ""

                val request = AddMachineRequest(

                    type = "seed drill",

                    model_name = modelName,

                    hp_range = null,

                    cutting_width = null,

                    working_width = null,

                    row_count = rowCount.toInt(),

                    price_per_hour = perHour.toIntOrNull(),
                    price_per_day = perDay.toIntOrNull(),
                    price_per_week = perWeek.toIntOrNull(),
                    price_per_month = perMonth.toIntOrNull(),

                    owner_name = ownerName,
                    owner_phone = ownerPhone,
                    owner_email = ownerEmail,
                    location = city
                )

                RetrofitClient.api.addMachine(request)

                    .enqueue(object : Callback<Map<String, String>> {

                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {

                            if (response.isSuccessful) {

                                Toast.makeText(
                                    context,
                                    "🌾 Seed Drill registered successfully!",
                                    Toast.LENGTH_LONG
                                ).show()

                                rowCount = ""
                                modelName = ""

                                perHour = ""
                                perDay = ""
                                perWeek = ""
                                perMonth = ""
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, String>>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                context,
                                "Failed: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}
@Composable
fun GiveRotavatorForm() {

    var workingWidth by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }

    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )

    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""

    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.rotavator_requirements),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = workingWidth,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    workingWidth = it
                }
            },
            label = {
                Text(stringResource(R.string.working_width_m))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.rotavator_model_name))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.pricing_details))

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    workingWidth.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }

                error = ""

                val request = AddMachineRequest(

                    type = "rotavator",

                    model_name = modelName,

                    hp_range = null,

                    cutting_width = null,

                    working_width = workingWidth.toDouble(),

                    row_count = null,

                    price_per_hour = perHour.toIntOrNull(),
                    price_per_day = perDay.toIntOrNull(),
                    price_per_week = perWeek.toIntOrNull(),
                    price_per_month = perMonth.toIntOrNull(),

                    owner_name = ownerName,
                    owner_phone = ownerPhone,
                    owner_email = ownerEmail,
                    location = city
                )

                RetrofitClient.api.addMachine(request)

                    .enqueue(object : Callback<Map<String, String>> {

                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {

                            if (response.isSuccessful) {

                                Toast.makeText(
                                    context,
                                    "🚜 Rotavator registered successfully!",
                                    Toast.LENGTH_LONG
                                ).show()

                                workingWidth = ""
                                modelName = ""

                                perHour = ""
                                perDay = ""
                                perWeek = ""
                                perMonth = ""
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, String>>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                context,
                                "Failed: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}

@Composable
fun PriceFields(
    perHour: String, onHour: (String) -> Unit,
    perDay: String, onDay: (String) -> Unit,
    perWeek: String, onWeek: (String) -> Unit,
    perMonth: String, onMonth: (String) -> Unit
) {

    fun onlyNumbers(input: String, update: (String) -> Unit) {
        if (input.all { it.isDigit() }) {
            update(input)
        }
    }

    OutlinedTextField(
        value = perHour,
        onValueChange = { onlyNumbers(it, onHour) },
        label = { Text(stringResource(R.string.price_per_hour)) },
        leadingIcon = { Text("₹") },   // 🔥 RUPEE SYMBOL
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perDay,
        onValueChange = { onlyNumbers(it, onDay) },
        label = { Text(stringResource(R.string.price_per_day)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perWeek,
        onValueChange = { onlyNumbers(it, onWeek) },
        label = { Text(stringResource(R.string.price_per_week)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perMonth,
        onValueChange = { onlyNumbers(it, onMonth) },
        label = { Text(stringResource(R.string.price_per_month)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MachineCard(
    emoji: String,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    value: String,
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandChange(!expanded) }
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        onExpandChange(false)
                    }
                )
            }
        }
    }
}
@Composable
fun RecommendationScreen(
    result: RecommendResponse?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedMachine by remember {
        mutableStateOf<Machine?>(null)
    }
    var selectedImageUrl by remember {
        mutableStateOf<String?>(null)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "✅ Recommendation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {

            result == null -> {

                Text(
                    "Loading...",
                    color = Color.Gray
                )
            }

            result.machines.isEmpty() -> {

                Text(
                    "❌ No machines found",
                    color = Color.Red
                )
            }

            else -> {

                Text(
                    text = "Recommended Range: ${result.prediction}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                result.machines
                    .sortedByDescending { it.recommended == true }
                    .forEach { machine ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    selectedMachine = machine
                                },

                            shape = RoundedCornerShape(16.dp),

                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                if (machine.recommended == true) {

                                    Text(
                                        text = "⭐ Recommended",
                                        color = Color(0xFFFFA000),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(4.dp))
                                }

                                Text(
                                    text = machine.model_name ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(8.dp))

                                machine.hp_range?.let {
                                    Text("⚙️ HP Range: $it HP")
                                }

                                machine.cutting_width?.let {
                                    Text("🔪 Cutting Width: $it ft")
                                }

                                machine.working_width?.let {
                                    Text("📏 Working Width: $it m")
                                }

                                machine.row_count?.let {
                                    Text("🌾 Row Count: $it")
                                }

                                Spacer(Modifier.height(8.dp))

                                Text("💰 Hour: ₹${machine.price_per_hour}")
                                Text("💰 Day: ₹${machine.price_per_day}")
                                Text("💰 Week: ₹${machine.price_per_week}")
                                Text("💰 Month: ₹${machine.price_per_month}")

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Tap to view owner details",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                machine.image_url?.let {

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            selectedImageUrl = machine.image_url
                                            Log.e("IMAGE", machine.image_url ?: "NULL")
                                        }
                                    ) {
                                        Text("📷 View Image")
                                    }
                                }
                            }
                        }
                    }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // 🔥 POPUP
    selectedMachine?.let { machine ->

        AlertDialog(

            onDismissRequest = {
                selectedMachine = null
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedMachine = null
                    }
                ) {
                    Text("Close")
                }
            },

            title = {
                Text("📞 Owner Details")
            },

            text = {

                Column {

                    Text("👤 Owner: ${machine.owner_name}")

                    Spacer(Modifier.height(8.dp))

                    Text("📞 Phone: ${machine.owner_phone}")

                    Spacer(Modifier.height(8.dp))

                    Text("📧 Email: ${machine.owner_email}")

                    Spacer(Modifier.height(8.dp))

                    Text("📍 Location: ${machine.location}")

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // 📞 CALL BUTTON
                        Button(
                            onClick = {

                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${machine.owner_phone}")
                                )

                                context.startActivity(intent)
                            }
                        ) {
                            Text("📞 Call")
                        }

                        // 📧 EMAIL BUTTON
                        Button(
                            onClick = {

                                val intent = Intent(Intent.ACTION_SEND).apply {

                                    type = "message/rfc822"

                                    putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(machine.owner_email)
                                    )

                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Regarding Machine Rental"
                                    )

                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
Hello ${machine.owner_name},

I am interested in renting your machine:

${machine.model_name}

Please share more details.

Thank you.
                """.trimIndent()
                                    )
                                }

                                context.startActivity(
                                    Intent.createChooser(intent, "Send Email")
                                )
                            }
                        ) {
                            Text("📧 Email")
                        }
                    }
                }
            }
        )
    }
    selectedImageUrl?.let {

        AlertDialog(

            onDismissRequest = {
                selectedImageUrl = null
            },

            confirmButton = {

                Button(
                    onClick = {
                        selectedImageUrl = null
                    }
                ) {
                    Text("Close")
                }
            },

            title = {
                Text("Machine Image")
            },

            text = {

                AsyncImage(

                    model = "http://192.168.0.166:8000/${it.removePrefix("/")}",

                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        )
    }
}
@Composable
fun MyMachinesScreen() {

    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    var editHpRange by remember {
        mutableStateOf("")
    }
    val phone = prefs.getString("phone", "") ?: ""
    var editCuttingWidth by remember { mutableStateOf("") }

    var editWorkingWidth by remember { mutableStateOf("") }

    var editRowCount by remember { mutableStateOf("") }
    var machines by remember {
        mutableStateOf<List<Machine>>(emptyList())
    }
    var selectedMachine by remember {
        mutableStateOf<Machine?>(null)
    }

    var editModelName by remember { mutableStateOf("") }

    var editPricePerHour by remember { mutableStateOf("") }
    var editPricePerDay by remember { mutableStateOf("") }
    var editPricePerWeek by remember { mutableStateOf("") }
    var editPricePerMonth by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {

        RetrofitClient.api.getMyMachines(phone)

            .enqueue(object : Callback<MyMachinesResponse> {

                override fun onResponse(
                    call: Call<MyMachinesResponse>,
                    response: Response<MyMachinesResponse>
                ) {

                    if (response.isSuccessful) {

                        machines =
                            response.body()?.machines ?: emptyList()
                    }
                }

                override fun onFailure(
                    call: Call<MyMachinesResponse>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        context,
                        "Failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "🚜 My Machines",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        if (machines.isEmpty()) {

            Text("No machines added yet")

        } else {

            machines.forEach { machine ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),

                    shape = RoundedCornerShape(16.dp),

                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = machine.model_name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            machine.type
                                ?.replace("_", " ")
                                ?.replaceFirstChar {
                                    it.uppercase()
                                }
                                ?: ""
                        )

                        machine.hp_range?.let {
                            Text("⚙️ HP: $it")
                        }

                        machine.cutting_width?.let {
                            Text("🔪 Cutting Width: $it ft")
                        }

                        machine.working_width?.let {
                            Text("📏 Working Width: $it m")
                        }

                        machine.row_count?.let {
                            Text("🌾 Row Count: $it")
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("💰 Hour: ₹${machine.price_per_hour}")
                        Text("💰 Day: ₹${machine.price_per_day}")
                        Text("💰 Week: ₹${machine.price_per_week}")
                        Text("💰 Month: ₹${machine.price_per_month}")
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selectedMachine = machine

                                editModelName =
                                    machine.model_name ?: ""

                                editPricePerHour =
                                    machine.price_per_hour?.toString() ?: ""

                                editPricePerDay =
                                    machine.price_per_day?.toString() ?: ""
                                editCuttingWidth =
                                    machine.cutting_width?.toString() ?: ""

                                editWorkingWidth =
                                    machine.working_width?.toString() ?: ""

                                editRowCount =
                                    machine.row_count?.toString() ?: ""
                                editPricePerWeek =
                                    machine.price_per_week?.toString() ?: ""
                                editHpRange =
                                    machine.hp_range?.toString() ?: ""
                                editPricePerMonth =
                                    machine.price_per_month?.toString() ?: ""
                            }
                        ) {
                            Text("✏️ Edit")
                        }
                    }
                }
            }
        }
    }
    selectedMachine?.let { machine ->

        AlertDialog(

            onDismissRequest = {
                selectedMachine = null
            },

            confirmButton = {

                Button(
                    onClick = {

                        val request = AddMachineRequest(

                            type = machine.type ?: "",

                            model_name = editModelName,

                            hp_range = editHpRange.toIntOrNull(),
                            cutting_width = editCuttingWidth.toDoubleOrNull(),

                            working_width = editWorkingWidth.toDoubleOrNull(),

                            row_count = editRowCount.toIntOrNull(),

                            price_per_hour = editPricePerHour.toIntOrNull(),
                            price_per_day = editPricePerDay.toIntOrNull(),
                            price_per_week = editPricePerWeek.toIntOrNull(),
                            price_per_month = editPricePerMonth.toIntOrNull(),

                            owner_name = "",
                            owner_phone = "",
                            owner_email = "",
                            location = machine.location ?: ""
                        )

                        RetrofitClient.api.updateMachine(
                            machine.id ?: 0,
                            request
                        ).enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {

                                Toast.makeText(
                                    context,
                                    "Updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                selectedMachine = null
                            }

                            override fun onFailure(
                                call: Call<Map<String, String>>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                ) {
                    Text("Save")
                }
            },

            dismissButton = {

                Button(
                    onClick = {
                        selectedMachine = null
                    }
                ) {
                    Text("Cancel")
                }
            },

            title = {
                Text("Edit Machine")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = editModelName,

                        onValueChange = {
                            editModelName = it
                        },
                        label = {
                            Text("Model Name")
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    when (machine.type) {

                        "tractor" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editHpRange,

                                onValueChange = {
                                    editHpRange = it
                                },

                                label = {
                                    Text("HP Range")
                                }
                            )
                        }

                        "harvester" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editCuttingWidth,

                                onValueChange = {
                                    editCuttingWidth = it
                                },

                                label = {
                                    Text("Cutting Width")
                                }
                            )
                        }

                        "rotavator" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editWorkingWidth,

                                onValueChange = {
                                    editWorkingWidth = it
                                },

                                label = {
                                    Text("Working Width")
                                }
                            )
                        }

                        "seed drill" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editRowCount,

                                onValueChange = {
                                    editRowCount = it
                                },

                                label = {
                                    Text("Row Count")
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPricePerHour,
                        onValueChange = {
                            editPricePerHour = it
                        },
                        label = {
                            Text("Price Per Hour")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPricePerDay,
                        onValueChange = {
                            editPricePerDay = it
                        },
                        label = {
                            Text("Price Per Day")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPricePerWeek,
                        onValueChange = {
                            editPricePerWeek = it
                        },
                        label = {
                            Text("Price Per Week")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPricePerMonth,
                        onValueChange = {
                            editPricePerMonth = it
                        },
                        label = {
                            Text("Price Per Month")
                        }
                    )
                }
            }
        )
    }
}

