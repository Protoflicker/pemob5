package com.example.myfirstkmpapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.launch

// Models
data class Note(val id: Int, val title: String, val content: String)

val dummyNotes = listOf(
    Note(1, "Belajar Compose", "Mempelajari navigasi multi-screen"),
    Note(2, "Tugas PAM Minggu 5", "Implementasi Bottom Nav & Drawer"),
    Note(3, "Ide Proyek", "Aplikasi manajemen tugas KMP")
)

// Routes
sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Notes : Screen("notes", Icons.Default.List, "Notes")
    object Favorites : Screen("favorites", Icons.Default.Favorite, "Favorites")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Int) = "edit_note/$noteId"
    }
}

// Main App
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Menu", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Notes") },
                        selected = currentRoute == Screen.Notes.route,
                        onClick = {
                            navController.navigate(Screen.Notes.route) { popUpTo(0) }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.List, null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Profile") },
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) { popUpTo(0) }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        ) {
            Scaffold(
                // Top Bar
                topBar = {
                    TopAppBar(
                        title = { Text("My Notes App") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                // Bottom Nav
                bottomBar = {
                    if (currentRoute in listOf(Screen.Notes.route, Screen.Favorites.route, Screen.Profile.route)) {
                        NavigationBar {
                            listOf(Screen.Notes, Screen.Favorites, Screen.Profile).forEach { screen ->
                                NavigationBarItem(
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(screen.icon!!, null) },
                                    label = { Text(screen.label!!) }
                                )
                            }
                        }
                    }
                },
                // FAB
                floatingActionButton = {
                    if (currentRoute == Screen.Notes.route) {
                        FloatingActionButton(onClick = { navController.navigate(Screen.AddNote.route) }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            ) { paddingValues ->
                // NavHost
                NavHost(
                    navController = navController,
                    startDestination = Screen.Notes.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Screen.Notes.route) {
                        NotesListScreen(onNoteClick = { id -> navController.navigate(Screen.NoteDetail.createRoute(id)) })
                    }
                    composable(Screen.Favorites.route) { FavoritesScreen() }
                    composable(Screen.Profile.route) { ProfileScreen() }
                    composable(
                        route = Screen.NoteDetail.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("noteId") ?: 0
                        NoteDetailScreen(
                            noteId = id,
                            onBackClick = { navController.popBackStack() },
                            onEditClick = { navController.navigate(Screen.EditNote.createRoute(id)) }
                        )
                    }
                    composable(Screen.AddNote.route) { AddNoteScreen(onBackClick = { navController.popBackStack() }) }
                    composable(
                        route = Screen.EditNote.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("noteId") ?: 0
                        EditNoteScreen(noteId = id, onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

// Screens
@Composable
fun NotesListScreen(onNoteClick: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(dummyNotes) { note ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onNoteClick(note.id) },
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(note.content, color = Color.Gray, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Favorites\n(Kosong)", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun NoteDetailScreen(noteId: Int, onBackClick: () -> Unit, onEditClick: () -> Unit) {
    val note = dummyNotes.find { it.id == noteId }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
        Spacer(modifier = Modifier.height(16.dp))
        if (note != null) {
            Text(note.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(note.content, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }
        }
    }
}

@Composable
fun AddNoteScreen(onBackClick: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
        Text("Tambah Catatan", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Isi") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) { Text("Simpan") }
    }
}

@Composable
fun EditNoteScreen(noteId: Int, onBackClick: () -> Unit) {
    val note = dummyNotes.find { it.id == noteId }
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
        Text("Edit Catatan", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Isi") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) { Text("Simpan") }
    }
}

// Profile
@Composable
fun ProfileScreen() {
    var isEditing by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var name by remember { mutableStateOf("Adi Septriansyah") }
    var nim by remember { mutableStateOf("123140021") }
    var bio by remember { mutableStateOf("Mahasiswa Teknik Informatika.") }
    var email by remember { mutableStateOf("adi.123140021.student.itera.ac.id") }
    var phone by remember { mutableStateOf("+62 812 7183 0293") }
    var location by remember { mutableStateOf("Bandar Lampung, Indonesia") }
    var projects by remember { mutableStateOf("• a)\n• b\n• c") }

    val scope = rememberCoroutineScope()
    val imagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays -> byteArrays.firstOrNull()?.let { profileImage = it.toImageBitmap() } }
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileHeader(name, nim, isEditing, profileImage, { imagePicker.launch() }, { name = it }, { nim = it })
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { isEditing = !isEditing }, modifier = Modifier.fillMaxWidth()) {
            Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isEditing) "Simpan" else "Edit")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ProfileSectionCard("Tentang Saya") {
            if (isEditing) OutlinedTextField(bio, { bio = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Bio") }, minLines = 3)
            else Text(bio, textAlign = TextAlign.Justify, color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        ProfileSectionCard("Kontak") {
            if (isEditing) {
                OutlinedTextField(email, { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
                OutlinedTextField(phone, { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Telepon") })
                OutlinedTextField(location, { location = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Lokasi") })
            } else {
                ContactInfoItem(Icons.Default.Email, email)
                ContactInfoItem(Icons.Default.Phone, phone)
                ContactInfoItem(Icons.Default.LocationOn, location)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (!isEditing) {
            OutlinedButton(onClick = { showDetails = !showDetails }, modifier = Modifier.fillMaxWidth()) { Text(if (showDetails) "Sembunyikan" else "Tampilkan Proyek") }
        }
        AnimatedVisibility(visible = showDetails || isEditing) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileSectionCard("Proyek") {
                    if (isEditing) OutlinedTextField(projects, { projects = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Proyek") }, minLines = 3)
                    else Text(projects, color = Color.DarkGray)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileHeader(name: String, nim: String, isEditing: Boolean, profileImage: ImageBitmap?, onImageClick: () -> Unit, onNameChange: (String) -> Unit, onNimChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).clickable(enabled = isEditing) { onImageClick() }, contentAlignment = Alignment.Center) {
            if (profileImage != null) Image(bitmap = profileImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ketuk foto untuk mengunggah", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(name, onNameChange, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(nim, onNimChange, label = { Text("NIM") }, modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("NIM: $nim", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ProfileSectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}

@Composable
fun ContactInfoItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 14.sp)
    }
}