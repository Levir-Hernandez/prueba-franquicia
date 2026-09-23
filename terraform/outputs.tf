output "api_url" {
  value = "http://${aws_eip.api.public_ip}:8080"
}

output "swagger_url" {
  value = "http://${aws_eip.api.public_ip}:8080/swagger-ui.html"
}

output "instance_id" {
  value = aws_instance.api.id
}
