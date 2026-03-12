package com.example.myfirstkmpapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

@Composable
fun App() {
    MaterialTheme {
        var isEditing by remember { mutableStateOf(false) }
        var showDetails by remember { mutableStateOf(false) }

        var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
        var name by remember { mutableStateOf("Adi Septriansyah") }
        var nim by remember { mutableStateOf("123140021") }
        var bio by remember { mutableStateOf("Mahasiswa Teknik Informatika yang antusias dalam pengembangan perangkat lunak. Saat ini berfokus pada Kotlin, Sistem Informasi Geografis (SIG), Manajemen Proyek, dan Kriptografi.") }
        var email by remember { mutableStateOf("adi.septriansyah@example.com") }
        var phone by remember { mutableStateOf("+62 812 3456 7890") }
        var location by remember { mutableStateOf("Bandar Lampung, Indonesia") }
        var projects by remember { mutableStateOf("• Aplikasi News Feed (Kotlin)\n• Analisis Spasial QGIS & PostGIS\n• Implementasi Rail Fence Cipher") }

        val scope = rememberCoroutineScope()
        val imagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            onResult = { byteArrays ->
                byteArrays.firstOrNull()?.let { bytes ->
                    profileImage = bytes.toImageBitmap()
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                name = name,
                nim = nim,
                isEditing = isEditing,
                profileImage = profileImage,
                onImageClick = { imagePicker.launch() },
                onNameChange = { name = it },
                onNimChange = { nim = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Simpan Profil" else "Edit Profil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(title = "Tentang Saya") {
                if (isEditing) {
                    OutlinedTextField(bio, { bio = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Deskripsi Diri") }, minLines = 3)
                } else {
                    Text(bio, textAlign = TextAlign.Justify, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(title = "Informasi Kontak") {
                if (isEditing) {
                    OutlinedTextField(email, { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(phone, { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Telepon") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(location, { location = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Lokasi") })
                } else {
                    ContactInfoItem(Icons.Default.Email, email)
                    ContactInfoItem(Icons.Default.Phone, phone)
                    ContactInfoItem(Icons.Default.LocationOn, location)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                OutlinedButton(
                    onClick = { showDetails = !showDetails },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showDetails) "Sembunyikan Proyek" else "Tampilkan Proyek Terbaru")
                }
            }

            AnimatedVisibility(visible = showDetails || isEditing) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileSectionCard(title = "Proyek Berjalan") {
                        if (isEditing) {
                            OutlinedTextField(projects, { projects = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Daftar Proyek") }, minLines = 3)
                        } else {
                            Text(projects, color = Color.DarkGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    name: String, nim: String, isEditing: Boolean, profileImage: ImageBitmap?,
    onImageClick: () -> Unit, onNameChange: (String) -> Unit, onNimChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(enabled = isEditing) { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            if (profileImage != null) {
                Image(bitmap = profileImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
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
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), shape = RoundedCornerShape(12.dp)) {
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
