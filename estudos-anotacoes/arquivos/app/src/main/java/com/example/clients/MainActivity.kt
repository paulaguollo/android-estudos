package com.example.clients   // <- igual ao teu package

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ================= 1. CORES, FONTE, DADOS =================
val Purple = Color(0xFF8B7CF6)
val Fundo = Color(0xFFF6F7FF)
val Cinza = Color(0xFF9CA3AF)
val CoresAvatar = listOf(Color(0xFFC6D82E), Color(0xFF9C27B0), Color(0xFF009688))

// res/font: inter_regular.ttf e inter_bold.ttf  (sem ficheiros: val Inter = FontFamily.Default)
val Inter = FontFamily(Font(R.font.inter_regular), Font(R.font.inter_bold, FontWeight.Bold))

data class Client(val id: Int, val nome: String, val email: String, val tel: String)

// Texto com a fonte Inter. Uso: Txt("Olá", 14, true, Color.Gray)
@Composable
fun Txt(
    texto: String, tam: Int = 14, bold: Boolean = false, cor: Color = Color.Black,
    modifier: Modifier = Modifier, align: TextAlign? = null
) {
    Text(
        texto, modifier = modifier, color = cor, fontSize = tam.sp, fontFamily = Inter,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = align
    )
}

// ================= 2. ACTIVITY + NAVEGAÇÃO =================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { App() } }
    }
}

@Composable
fun App() {
    val clientes = remember {
        mutableStateListOf(
            Client(1, "Ana Maria", "anamaria@gmail.com", "965111872"),
            Client(2, "José Silva", "josesilva@gmail.com", "912345678"),
            Client(3, "João Maria", "joaomaria@gmail.com", "913456789"),
            Client(4, "Pedro Medeiros", "pedrotomemedeiros@gmail.com", "934567890")
        )
    }
    var tela by remember { mutableStateOf("lista") }      // lista | novo | detalhe | editar
    var sel by remember { mutableStateOf<Client?>(null) } // cliente escolhido

    BackHandler(tela != "lista") { tela = if (tela == "editar") "detalhe" else "lista" }

    when (tela) {
        "lista" -> Lista(clientes, onNovo = { tela = "novo" }, onAbrir = { sel = it; tela = "detalhe" })

        "novo" -> Formulario(
            "Novo Cliente", "Preencha os dados do novo cliente",
            Client(0, "", "", ""), "Criar Cliente", onVoltar = { tela = "lista" }
        ) { c ->
            clientes.add(c.copy(id = (clientes.maxOfOrNull { it.id } ?: 0) + 1))
            tela = "lista"
        }

        "detalhe" -> Detalhe(
            sel!!, onVoltar = { tela = "lista" }, onEditar = { tela = "editar" },
            onEliminar = { clientes.remove(sel!!); tela = "lista" }
        )

        "editar" -> Formulario(
            "Editar Cliente", "Atualize os dados abaixo",
            sel!!, "Guardar Alterações", onVoltar = { tela = "detalhe" }
        ) { c ->
            clientes[clientes.indexOfFirst { it.id == c.id }] = c
            sel = c
            tela = "detalhe"
        }
    }
}

// ================= 3. TELA LISTA =================
@Composable
fun Lista(clientes: List<Client>, onNovo: () -> Unit, onAbrir: (Client) -> Unit) {
    // ordena A-Z e agrupa pela 1ª letra
    val grupos = clientes.sortedBy { it.nome }.groupBy { it.nome.first().uppercaseChar() }

    Box(Modifier.fillMaxSize().background(Fundo)) {
        Column {
            Row(
                Modifier.fillMaxWidth().background(Color.White).statusBarsPadding().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Txt("Clientes", 18, true)
                Icon(Icons.Default.Search, null)
            }
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                grupos.forEach { (letra, lista) ->
                    item { Txt(letra.toString(), 14, false, Cinza, Modifier.padding(vertical = 8.dp)) }
                    items(lista) { c ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onAbrir(c) },
                            shape = RoundedCornerShape(16.dp), color = Color.White
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape)
                                        .background(CoresAvatar[c.nome.first().code % 3]),
                                    contentAlignment = Alignment.Center
                                ) { Txt(c.nome.first().uppercase(), 16, true, Color.White) }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Txt(c.nome, 13, true)
                                    Txt(c.email, 11, false, Cinza)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Cinza)
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNovo, containerColor = Purple, contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Txt("ADICIONAR", 13, true, Color.White)
        }
    }
}

// ================= 4. TELA DETALHE (+ modal) =================
@Composable
fun Detalhe(c: Client, onVoltar: () -> Unit, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val ctx = LocalContext.current
    var dialogo by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Fundo)) {
        Column {
            // cabeçalho roxo
            Column(
                Modifier.fillMaxWidth().background(Purple).statusBarsPadding().padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.clickable { onVoltar() }, tint = Color.White)
                    Icon(Icons.Default.Delete, null, Modifier.clickable { dialogo = true }, tint = Color.White)
                }
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) { Txt(c.nome.first().uppercase(), 32, true, Purple) }
                Spacer(Modifier.height(12.dp))
                Txt(c.nome, 16, true, Color.White)
            }
            // contactos
            Column(Modifier.padding(16.dp)) {
                Txt("DADOS DE CONTACTO", 11, true, Purple, Modifier.padding(bottom = 8.dp))
                CartaoInfo(Icons.Default.Email, "Correio Eletrónico", c.email)
                Spacer(Modifier.height(8.dp))
                CartaoInfo(Icons.Default.Phone, "Telemóvel / Contacto", c.tel)
                Spacer(Modifier.height(20.dp))
                Txt("GESTÃO", 11, true, Purple, Modifier.padding(bottom = 8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.tel}")))
                    },
                    shape = RoundedCornerShape(12.dp), color = Color.White
                ) {
                    Row(
                        Modifier.padding(14.dp), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, null, tint = Purple)
                        Spacer(Modifier.width(8.dp))
                        Txt("Ligar para o Cliente", 13, true, Purple)
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onEditar, containerColor = Purple, contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Txt("EDITAR PERFIL", 13, true, Color.White)
        }
        if (dialogo) AlertDialog(
            onDismissRequest = { dialogo = false },
            icon = { Icon(Icons.Default.Info, null, tint = Purple) },
            title = { Txt("Eliminar Cliente", 18, true, modifier = Modifier.fillMaxWidth(), align = TextAlign.Center) },
            text = {
                Txt(
                    "Tem a certeza de que deseja eliminar este cliente? Esta ação é permanente.",
                    12, false, Cinza, Modifier.fillMaxWidth(), TextAlign.Center
                )
            },
            dismissButton = { TextButton(onClick = { dialogo = false }) { Txt("Cancelar", 13, false, Color.Gray) } },
            confirmButton = {
                Button(
                    onClick = { dialogo = false; onEliminar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) { Txt("Confirmar", 13, true, Color.White) }
            }
        )
    }
}

// cartão branco com ícone + legenda + valor
@Composable
fun CartaoInfo(icone: androidx.compose.ui.graphics.vector.ImageVector, legenda: String, valor: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, null, tint = Purple)
            Column(Modifier.padding(start = 12.dp)) {
                Txt(legenda, 10, false, Cinza)
                Txt(valor, 13, true)
            }
        }
    }
}

// ================= 5. TELA FORMULÁRIO (Novo e Editar) =================
@Composable
fun Formulario(
    titulo: String, subtitulo: String, inicial: Client, botao: String,
    onVoltar: () -> Unit, onGuardar: (Client) -> Unit
) {
    var nome by remember { mutableStateOf(inicial.nome) }
    var email by remember { mutableStateOf(inicial.email) }
    var tel by remember { mutableStateOf(inicial.tel) }

    Column(Modifier.fillMaxSize().background(Fundo)) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.clickable { onVoltar() })
            Txt(titulo, 18, true, modifier = Modifier.padding(start = 16.dp))
        }
        Column(Modifier.padding(20.dp)) {
            Txt(subtitulo, 11, false, Cinza)
            Campo("Nome Completo", nome, { nome = it }) { Icon(Icons.Default.Person, null, tint = Purple) }
            Campo("Email", email, { email = it }) { Txt("@", 18, true, Purple) }
            Campo("Telemóvel / Contacto", tel, { tel = it }) { Icon(Icons.Default.Phone, null, tint = Purple) }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { if (nome.isNotBlank()) onGuardar(inicial.copy(nome = nome, email = email, tel = tel)) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Txt(botao, 13, true, Color.White) }
        }
    }
}

// rótulo + caixa de texto branca arredondada com ícone
@Composable
fun Campo(rotulo: String, valor: String, aoMudar: (String) -> Unit, icone: @Composable () -> Unit) {
    Txt(rotulo, 11, true, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
    TextField(
        valor, aoMudar, Modifier.fillMaxWidth(), leadingIcon = icone, singleLine = true,
        shape = RoundedCornerShape(12.dp), textStyle = TextStyle(fontFamily = Inter),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
        )
    )
}
