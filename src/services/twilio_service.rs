use twilio::{Client, OutboundMessage};

pub async fn send_sms(message: &str, config: &crate::config::Config) -> Result<(), Box<dyn std::error::Error>> {
    let client = Client::new(&config.twilio_account_sid, &config.twilio_auth_token);
    let message = OutboundMessage::new(&config.twilio_from_number, &config.admin_phone_number, message);
    client.send_message(message).await?;
    Ok(())
}