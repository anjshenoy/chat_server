require 'eventmachine'

class Server < EM::Connection
  attr_accessor :queue

  def receive_data(data)
    publish(data)
  end

  def publish(data)
    self.queue.each do |sock|
      sock.send_data(data) unless sock == self
    end
  end
end

EM.run do
  q = []
  EM.start_server("0.0.0.0", 9999, Server) do |server|
    q.push server
    server.queue = q
  end
end
