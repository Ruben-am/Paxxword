package com.rubenalba.paxxword.ui.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rubenalba.paxxword.domain.model.AccountModel

@Composable
fun AccountItem(
    account: AccountModel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val initial = account.serviceName.firstOrNull()?.uppercase() ?: "?"

            val color = remember(account.serviceName) {
                val hash = account.serviceName.hashCode()
                Color(hash).copy(alpha = 1f).let {
                    Color(it.red, it.green, it.blue, 1f)
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Service name
                Text(
                    text = account.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                // User/email
                Text(
                    text = account.username.ifEmpty { account.email },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountItemPreview() {
    val fakeAccount = AccountModel(
        id = 1,
        serviceName = "Netflix",
        username = "Usuario",
        email = "usuario@test.com",
        password = "1234",
        folderId = null
    )

    AccountItem(
        account = fakeAccount,
        onClick = {}
    )
}