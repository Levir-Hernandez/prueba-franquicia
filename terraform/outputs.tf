########################################################################
# Salidas
#
# Lo que se necesita saber tras un "apply" correcto.
########################################################################

output "api_urls" {
  description = "URL publica de cada microservicio"
  value       = { for nombre, s in local.servicios : nombre => "http://${aws_eip.api.public_ip}:${s.puerto}" }
}

output "swagger_urls" {
  description = "Documentacion interactiva de cada microservicio"
  value       = { for nombre, s in local.servicios : nombre => "http://${aws_eip.api.public_ip}:${s.puerto}/swagger-ui.html" }
}

output "ecr_registry" {
  description = "Registro de ECR donde publicar las imagenes"
  value       = local.registro
}

output "ecr_repositories" {
  description = "Repositorio de cada servicio"
  value       = { for nombre, r in aws_ecr_repository.servicio : nombre => r.repository_url }
}

output "atlas_clusters" {
  description = "Host de conexion de cada cluster de MongoDB Atlas"
  value       = { for nombre, c in mongodbatlas_advanced_cluster.servicio : nombre => c.connection_strings[0].standard_srv }
}

output "instance_id" {
  description = "Instancia (acceso por SSM Session Manager, sin SSH)"
  value       = aws_instance.api.id
}
