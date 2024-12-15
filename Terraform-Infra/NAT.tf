

#NAT is used for Labmbda Function as it is deployed in private network and cant access internet gateway as
#is has to make call to send grid third party API's

resource "aws_eip" "nat" {
  vpc   = true
  count = 1
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat[0].id
  subnet_id     = aws_subnet.public[0].id

  tags = {
    Name = "Main NAT Gateway"
  }
}

resource "aws_route" "private_internet_access" {
  route_table_id         = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.main.id
}