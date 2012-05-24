require 'eventmachine'

class Client < EM::Connection
  attr_accessor :name

  def receive_data(data)
    puts data
  end

  def readline
    until(((x = $stdin.readline) =~ /^bye/) == 0) do
      yield x
    end
    EM.stop
  end

end

EM.run do
  EM.connect("0.0.0.0", 9999, Client) do |client|
    client.name = ARGV[0]

    EM.defer do
      client.readline do |data|
        client.send_data("#{client.name}: #{data}")
      end
    end
  end
end
